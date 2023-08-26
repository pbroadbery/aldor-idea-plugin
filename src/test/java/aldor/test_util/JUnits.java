package aldor.test_util;

import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
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
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Fun things that unit tests can use.
 */
public final class JUnits {
    public static final TestRule setLogToInfoTestRule = prePostTestRule(JUnits::setLogToInfo, JUnits::setLogToInfo); // FIXME: Really should trace logging state correctly
    public static final TestRule setLogToDebugTestRule = prePostTestRule(JUnits::setLogToDebug, JUnits::setLogToInfo);
    private static final Logger LOG = Logger.getInstance(JUnits.class);

    public static Runnable setLogToDebug() {
        return isCIBuild() ? () -> {} : setLogLevel(LogLevel.DEBUG);
    }

    public static Runnable setLogToInfo() {
        return isCIBuild() ? () -> {} : setLogLevel(LogLevel.INFO);
    }

    @NotNull
    public static <T> T fail() {
        Assert.fail();
        //noinspection ReturnOfNull
        return null;
    }

    private static Runnable setLogLevel(LogLevel level) {
        /*
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

        return () -> LogManager.getRootLogger().removeAppender(appender);
        */
        Logger.getInstance("").setLevel(level);
        return () -> {
            var logger = Logger.getInstance("");
            logger.setLevel(level);
        };
    }

    public enum JpsDebuggingState { ON, OFF }

    public static void enableJpsDebugging(JpsDebuggingState debuggingState) {
        //noinspection AccessOfSystemProperties
        System.setProperty("compiler.process.debug.port", "28771");
        BuildManager.getInstance().setBuildProcessDebuggingEnabled(debuggingState == JpsDebuggingState.ON);
    }

    public static boolean isCIBuild() {
        //noinspection AccessOfSystemProperties
        return Objects.equals(System.getProperty("aldor.build.skip_ci"), "true");
    }

    public static TestRule prePostTestRule(UnsafeRunnable pre, UnsafeRunnable post) {
        return (statement, description) -> prePostStatement(pre, post, statement);
    }

    public static Statement prePostStatement(UnsafeRunnable pre, UnsafeRunnable post, Statement statement) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                pre.run();
                Throwable fail = null;
                try {
                    statement.evaluate();
                } finally {
                    try {
                        post.run();
                    } catch (RuntimeException e) {
                        System.out.println("Exception at end of rule " + e.getMessage());
                        fail = e;
                    }
                }
                if (fail != null) {
                    throw fail;
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
        ProgramRunner<?> runner = ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, settings.getConfiguration());
        assert runner != null;
        ExecutionEnvironment environment = new ExecutionEnvironment(executor, runner, settings, project);
        JavaCommandLine state = (JavaCommandLine) configuration.getState(executor, environment);
        assert state != null;
        LOG.info("CommandLine: " + state.getJavaParameters().toCommandLine());
        JavaParameters parameters = state.getJavaParameters();
        parameters.setUseDynamicClasspath(project);
        GeneralCommandLine commandLine = parameters.toCommandLine();

        OSProcessHandler process = new OSProcessHandler(commandLine);

        ProcessOutput processOutput = new ProcessOutput();
        process.addProcessListener(new ProcessAdapter() {

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                processOutput.exitCode(event.getExitCode());
            }

            @SuppressWarnings("ObjectEquality")
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                LOG.info("Text available: " + outputType + " " + event.getText());
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
        boolean flg = process.waitFor();
        process.destroyProcess();
        assertTrue(flg);
        return processOutput;
    }

    public static class ProcessOutput {
        public List<String> out = new ArrayList<>();
        public List<String> err = new ArrayList<>();
        public List<String> sys = new ArrayList<>();
        public List<ServiceMessage> messages = new ArrayList<>();
        public int exitCode = Integer.MIN_VALUE;

        public void exitCode(int exitCode) {
            this.exitCode = exitCode;
        }
    }

    public static class TearDownItem {
        private final Runnable runnable;
        private final Throwable alloc = new Throwable();

        public TearDownItem() {
            this.runnable = () -> {};
        }

        public TearDownItem(Runnable r) {
            this.runnable = () -> {
                try {
                    r.run();
                }
                catch (Throwable t) {
                    LOG.warn("Creation point: ", alloc);
                    LOG.warn("Error ", t);
                    throw t;
                }
            };
        }

        public TearDownItem with(Runnable r) {
            return new TearDownItem(() -> { runnable.run(); r.run(); });
        }

        public void tearDown() {
            this.runnable.run();
        }
    }

    public interface TearDownAware {
        JUnit3TearDown tearDownTracker();

        default void withTearDown(UnsafeRunnable r) {
            tearDownTracker().add(r);
        }

        default void withSafeTearDown(Runnable r) {
            tearDownTracker().add(() -> r.run());
        }

        default void loggedTearDown(String name, UnsafeRunnable r) {
            tearDownTracker().addLogged(name, r);
        }

    }

    public static class JUnit3TearDown {
        private final List<UnsafeRunnable> tearDowns = new LinkedList<>();
        private boolean isSetUp = false;

        public void setup(Class<? extends TestCase> clzz, UnsafeRunnable teardown) {
            if (this.isSetUp) {
                throw new RuntimeException("Already set up");
            }
            this.isSetUp = true;
            addLogged("Parent (" + clzz.getCanonicalName() + ")", teardown);
        }

        public void add(UnsafeRunnable runnable) {
            tearDowns.add(0, runnable);
        }

        void tearDown() {
            for (UnsafeRunnable r : tearDowns) {
                //noinspection OverlyBroadCatchBlock
                try {
                    r.run();
                } catch (Exception e) {
                    LOG.error("Failed to close down test");
                    LOG.error(e);
                }
            }
        }

        public void addLogged(String name, UnsafeRunnable r) {
            add(() ->{
                LOG.info("Tear down " + name);
                boolean ok = false;
                try {
                    r.run();
                    ok = true;
                }
                finally {
                    LOG.info("Tear down complete" + name + " (OK: "+ ok + ")");
                }

            });
        }
    }
}
