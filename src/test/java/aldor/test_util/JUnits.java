package aldor.test_util;

import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.helpers.UtilLoggingLevel.INFO;

/**
 * Fun things that unit tests can use.
 */
public final class JUnits {
    public static final TestRule setLogToInfoTestRule = prePostTestRule(JUnits::setLogToInfo, LogManager::resetConfiguration);

    public static void setLogToDebug() {
        setLogLevel(DEBUG);
    }

    public static void setLogToInfo() {
        setLogLevel(INFO);
    }

    private static void setLogLevel(Level level) {
        LogManager.resetConfiguration();
        EdtTestUtil.runInEdtAndWait(() -> {
            String threadName = Thread.currentThread().getName();
            int idx = threadName.indexOf(' ');
            if (idx > 0) {
                Thread.currentThread().setName(threadName.substring(0, threadName.indexOf(' ')));
            }
        });
        Appender appender = new ConsoleAppender(new PatternLayout("%r [%t] %p %.40c %x - %m%n"));
        appender.setName("Console");
        LogManager.getRootLogger().addAppender(appender);
        LogManager.getRootLogger().setLevel(level);
        Logger.setFactory(TestLoggerFactory.class);
    }

    public static void enableJpsDebugging(boolean enabled) {
        System.setProperty("compiler.process.debug.port", "28771");
        BuildManager.getInstance().setBuildProcessDebuggingEnabled(enabled);
    }



    public static TestRule prePostTestRule(UnsafeRunnable pre, UnsafeRunnable post) {
        return (statement, description) -> prePostStatement(pre, post, statement);
    }

    public static Statement prePostStatement(UnsafeRunnable pre, UnsafeRunnable post, Statement statement) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                pre.run();
                try {
                    statement.evaluate();
                } finally {
                    try {
                        post.run();
                    } catch (RuntimeException e) {
                        System.out.println("Exception at end of rule " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
        };
    }

    public static TestRule fixtureRule(IdeaTestFixture fixture) {
        return prePostTestRule(fixture::setUp, fixture::tearDown);
    }

    public static TestRule swingThreadTestRule() {
        return new SwingThreadTestRule();
    }

    public static SafeCloseable asResource(UnsafeRunnable pre, UnsafeRunnable post) {
        try {
            pre.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new SafeCloseable() {
            @Override
            public void close() {
                try {
                    post.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static SafeCloseable withSdk(Sdk sdk) {
        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
        UnsafeRunnable open = () -> ApplicationManager.getApplication().runWriteAction(() -> jdkTable.addJdk(sdk));
        UnsafeRunnable close = () -> ApplicationManager.getApplication().runWriteAction(() -> jdkTable.removeJdk(sdk));
        return asResource(open, close);
    }

    public static ProcessOutput doStartTestsProcess(RunConfiguration configuration) throws ExecutionException {
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        Project project = configuration.getProject();
        RunnerAndConfigurationSettingsImpl
                settings = new RunnerAndConfigurationSettingsImpl(RunManagerImpl.getInstanceImpl(project), configuration, false);
        ProgramRunner<?> runner = ProgramRunnerUtil.getRunner(DefaultRunExecutor.EXECUTOR_ID, settings);
        assert runner != null;
        ExecutionEnvironment
                environment = new ExecutionEnvironment(executor, runner, settings, project);
        JavaCommandLine state = (JavaCommandLine) configuration.getState(executor, environment);
        assert state != null;
        JavaParameters parameters = state.getJavaParameters();
        parameters.setUseDynamicClasspath(project);
        GeneralCommandLine commandLine = parameters.toCommandLine();

        OSProcessHandler process = new OSProcessHandler(commandLine);

        ProcessOutput processOutput = new ProcessOutput();
        process.addProcessListener(new ProcessAdapter() {

            @SuppressWarnings("ObjectEquality")
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @SuppressWarnings("rawtypes") @NotNull Key outputType) {
                String text = event.getText();
                if (StringUtil.isEmptyOrSpaces(text)) {
                    return;
                }
                try {
                    if (outputType == ProcessOutputTypes.STDOUT) {
                        ServiceMessage serviceMessage = ServiceMessage.parse(text.trim());
                        if (serviceMessage == null) {
                            processOutput.out.add(text);
                        } else {
                            processOutput.messages.add(serviceMessage);
                        }
                    }

                    if (outputType == ProcessOutputTypes.SYSTEM) {
                        processOutput.sys.add(text);
                    }

                    if (outputType == ProcessOutputTypes.STDERR) {
                        processOutput.err.add(text);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    System.err.println(text);
                }
            }
        });
        process.startNotify();
        process.waitFor();
        process.destroyProcess();

        return processOutput;
    }

    public static class ProcessOutput {
        public List<String> out = new ArrayList<>();
        public List<String> err = new ArrayList<>();
        public List<String> sys = new ArrayList<>();
        public List<ServiceMessage> messages = new ArrayList<>();

    }

}
