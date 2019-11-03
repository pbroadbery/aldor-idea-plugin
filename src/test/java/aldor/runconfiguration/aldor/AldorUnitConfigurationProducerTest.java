package aldor.runconfiguration.aldor;

import aldor.psi.AldorExportDecl;
import aldor.psi.AldorWith;
import aldor.runconfiguration.MyMapDataContext;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.Executor;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;

import java.util.concurrent.CountDownLatch;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorUnitConfigurationProducerTest extends LightPlatformCodeInsightFixtureTestCase {

    private final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/aldorgit/opt");

    @Override
    public void setUp() throws Exception {
        JUnits.setLogToDebug();
        super.setUp();
        Assume.assumeTrue(directory.isPresent());
    }

    public void testRunSimpleConfiguration() throws ExecutionException {
        JUnits.setLogToInfo();
        VirtualFile file = createFile(getSourceRoot(), "foo.as",
                "#include \"aldor.as\"\n" +
                        "#pile\n" +
                        "export FooTest to Foreign Java(\"aldor.test\")\n" +
                        "FooTest: with\n" +
                        "    test: () -> ()\n" +
                        "== add \n" +
                        "    test() == never\n" +
                        "\n");

        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);
        Assert.assertNotNull(whole);
        AldorWith elt = PsiTreeUtil.findChildOfType(whole, AldorWith.class);
        Assert.assertNotNull(elt);
        MyMapDataContext dataContext = new MyMapDataContext();
        dataContext.put("module", myModule);
        dataContext.put("Location", new PsiLocation<>(elt));
        dataContext.put("project", getProject());

        ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
        System.out.println("Context: " + runContext.getLocation() + " " + runContext.getConfiguration());
        Assert.assertNotNull(runContext.getConfiguration());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

        Assert.assertEquals("FooTest (foo.as)", runnerAndConfigurationSettings.getName());
    }

    public void testStartOutside() throws ExecutionException {
        JUnits.setLogToInfo();
        VirtualFile file = createFile(getSourceRoot(), "foo.as",
                "#include \"aldor.as\"\n" +
                        "#pile\n" +
                        "export FooTest to Foreign Java(\"aldor.test\")\n" +
                        "FooTest: with\n" +
                        "    test: () -> ()\n" +
                        "== add \n" +
                        "    test() == never\n" +
                        "\n");

        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);
        Assert.assertNotNull(whole);
        PsiElement elt = PsiTreeUtil.findChildOfType(whole, AldorExportDecl.class);
        Assert.assertNotNull(elt);
        MyMapDataContext dataContext = new MyMapDataContext();
        dataContext.put("module", myModule);
        dataContext.put("Location", new PsiLocation<>(elt));
        dataContext.put("project", getProject());
        ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        Assert.assertNull(context.getConfigurationsFromContext());
    }

    private void executeRunner(RunnerAndConfigurationSettings settings) throws ExecutionException {
        ExecutionTargetManager.canRun(settings, ExecutionTargetManager.getActiveTarget(getProject()));
        Assert.assertTrue(settings.getName().contains("foo"));
        RunConfiguration runConfiguration = settings.getConfiguration();

        JUnits.ProcessOutput output = JUnits.doStartTestsProcess(runConfiguration);

        LOG.info("Output: " + output.out);
        LOG.info("Error: " + output.err);
        LOG.info("Sys: " + output.sys);
    }


    @Ignore("Disabled until I work out how to free the editor safely")
    public void testRun() throws ExecutionException, InterruptedException {
        JUnits.setLogToInfo();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "#include \"aldor.as\"\nFooTest: with == add\n");

        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);
        Assert.assertNotNull(whole);
        MyMapDataContext dataContext = new MyMapDataContext();
        dataContext.put("module", myModule);
        dataContext.put("Location", new PsiLocation<>(whole));
        dataContext.put("project", getProject());

        ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
        System.out.println("Context: " + runContext.getLocation() + " " + runContext.getConfiguration());
        Assert.assertNotNull(runContext.getConfiguration());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

        ExecutionTargetManager.canRun(runnerAndConfigurationSettings, ExecutionTargetManager.getActiveTarget(getProject()));
        Assert.assertTrue(runnerAndConfigurationSettings.getName().contains("foo"));

        RunConfiguration runConfiguration = runnerAndConfigurationSettings.getConfiguration();
        Assert.assertNotNull(runConfiguration.getConfigurationEditor());

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        ProgramRunner<?> runner = ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, runConfiguration);
        Assert.assertNotNull(runner);
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executor, runner, runnerAndConfigurationSettings, getProject());

        final Ref<RunContentDescriptor> descriptorBox = new Ref<>();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutionConsole console = null;
        try {
            runner.execute(executionEnvironment, new ProgramRunner.Callback() {
                @Override
                public void processStarted(RunContentDescriptor descriptor) {
                    descriptor.getProcessHandler().addProcessListener(new ProcessAdapter() {
                        @Override
                        public void processTerminated(@NotNull ProcessEvent event) {
                            latch.countDown();
                        }
                    });
                    descriptorBox.set(descriptor);
                }
            });
            descriptorBox.get().getProcessHandler().waitFor();
            console = descriptorBox.get().getExecutionConsole();

        } catch (RuntimeException e) {
            LOG.error("ouch", e);
            throw e;
        } finally {
   /*         Assert.assertNotNull(descriptorBox.get());
            RunContentDescriptor descriptor = descriptorBox.get();

            Assert.assertNotNull(descriptor.getProcessHandler());
            Assert.assertTrue(descriptor.getProcessHandler().waitFor());
            if (console != null) {
                console.dispose();
            }
     */
        }
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptorWithAldorUnit(directory.path());
    }
}