package aldor.runconfiguration.aldor;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetType;
import com.intellij.diagnostic.logging.OutputFileUtil;
import com.intellij.execution.CantRunException;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteConnectionCreator;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.ArgumentFileFilter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testDiscovery.JavaAutoRunManager;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.TestProxyRoot;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.autotest.AbstractAutoTestManager;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAldorUnitRunnableState<T
        extends ModuleBasedConfiguration<? extends RunConfigurationModule, Element>
                & LocatableConfiguration
                & SMRunnerConsolePropertiesProvider> extends JavaCommandLineState implements RemoteConnectionCreator {
    private static final Logger LOG = Logger.getInstance(AbstractAldorUnitRunnableState.class);
    private final List<ArgumentFileFilter> myArgumentFileFilters = new ArrayList<>(); // TODO: Where is this used?
    protected File myTempFile = null;
    protected File myWorkingDirsFile = null;
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private RemoteConnectionCreator remoteConnectionCreator = null;
    protected ServerSocket myServerSocket = null;

    protected AbstractAldorUnitRunnableState(@NotNull ExecutionEnvironment environment) {
        super(environment);
    }

    public void setRemoteConnectionCreator(RemoteConnectionCreator remoteConnectionCreator) {
        this.remoteConnectionCreator = remoteConnectionCreator;
    }


    @Override
    protected final JavaParameters createJavaParameters() throws CantRunException {
        final JavaParameters javaParameters = new JavaParameters();

        Sdk jdk = selectJdk();
        if (jdk == null) {
            throw new CantRunException("No JDK specified");
        }
        javaParameters.setJdk(jdk);

        javaParameters.getClassPath().addFirst(JavaSdkUtil.getIdeaRtJarPath());
        configureClasspath(javaParameters);
        configureVMParameters(javaParameters);
        configureParameters(javaParameters);
        return javaParameters;
    }

    protected abstract void configureVMParameters(JavaParameters javaParameters);

    protected abstract void configureParameters(JavaParameters javaParameters);

    @Nullable
    private Sdk selectJdk() {
        final Module module = getConfiguration().getConfigurationModule().getModule();
        if (module == null) {
            return null;
        }
        AldorFacet facet = FacetManager.getInstance(module).getFacetByType(AldorFacetType.instance().getId());
        if (facet == null) {
            return null;
        }
        String jdkName = facet.getConfiguration().getState().javaSdkName();
        LOG.info("Jdk " + jdkName + " " + ProjectJdkTable.getInstance().findJdk(jdkName));
        return ProjectJdkTable.getInstance().findJdk(jdkName);
    }

    protected final void configureClasspath(final JavaParameters javaParameters) throws CantRunException {
        configureRTClasspath(javaParameters);
    }

    protected abstract void configureRTClasspath(JavaParameters javaParameters) throws CantRunException;

    @Nullable
    @Override
    public RemoteConnection createRemoteConnection(ExecutionEnvironment environment) {
        return (remoteConnectionCreator == null) ? null : remoteConnectionCreator.createRemoteConnection(environment);
    }

    @Override
    public boolean isPollConnection() {
        return false;
    }


    protected abstract String getFrameworkName();

    @NotNull protected abstract T getConfiguration();

    protected abstract boolean isIdBasedTestTree();

    @NotNull protected abstract OSProcessHandler createHandler(Executor executor) throws ExecutionException;

    protected void deleteTempFiles() {
        if (myTempFile != null) {
            FileUtil.delete(myTempFile);
        }

        if (myWorkingDirsFile != null) {
            FileUtil.delete(myWorkingDirsFile);
        }
    }


    @NotNull
    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        final RunnerSettings runnerSettings = getRunnerSettings();

        final SMTRunnerConsoleProperties testConsoleProperties = getConfiguration().createTestConsoleProperties(executor);
        testConsoleProperties.setIdBasedTestTree(isIdBasedTestTree());
        testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

        final BaseTestsOutputConsoleView consoleView = SMTestRunnerConnectionUtil.createConsole(getFrameworkName(), testConsoleProperties);
        final SMTestRunnerResultsForm viewer = ((SMTRunnerConsoleView)consoleView).getResultsViewer();
        Disposer.register(getConfiguration().getProject(), consoleView);

        //ToolWindow runTool = ToolWindowManager.getInstance(getConfiguration().getProject()).getToolWindow("Run");



        final OSProcessHandler handler = createHandler(executor);

        for (ArgumentFileFilter filter : myArgumentFileFilters) {
            consoleView.addMessageFilter(filter);
        }

        consoleView.attachToProcess(handler);
        final AbstractTestProxy root = viewer.getRoot();
        if (root instanceof TestProxyRoot) {
            ((TestProxyRoot)root).setHandler(handler);
        }
        handler.addProcessListener(new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                if (getConfiguration().isSaveOutputToFile()) {
                    final File file = OutputFileUtil.getOutputFile(getConfiguration());
                    root.setOutputFilePath((file != null) ? file.getAbsolutePath() : null);
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                Runnable runnable = () -> {
                    root.flushOutputFile();
                    deleteTempFiles();
                    clear();
                };
                UIUtil.invokeLaterIfNeeded(runnable);
                handler.removeProcessListener(this);
            }
        });

        AbstractRerunFailedTestsAction rerunFailedTestsAction = testConsoleProperties.createRerunFailedTestsAction(consoleView);
        LOG.assertTrue(rerunFailedTestsAction != null);
        rerunFailedTestsAction.setModelProvider(() -> viewer);

        final DefaultExecutionResult result = new DefaultExecutionResult(consoleView, handler);
        result.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction() {
            @Override
            public boolean isDelayApplicable() {
                return false;
            }

            @Override
            public AbstractAutoTestManager getAutoTestManager(Project project) {
                return JavaAutoRunManager.getInstance(project);
            }
        });

        JavaRunConfigurationExtensionManager.getInstance().attachExtensionsToProcess(getConfiguration(), handler, runnerSettings);
        return result;
    }

    protected void createServerSocket(JavaParameters javaParameters) {
        try {
            //noinspection MagicNumber
            myServerSocket = new ServerSocket(0, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
            javaParameters.getProgramParametersList().add("-socket" + myServerSocket.getLocalPort());
        }
        catch (IOException e) {
            LOG.error(e);
        }
    }

}

