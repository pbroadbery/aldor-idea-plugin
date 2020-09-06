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
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;

import java.util.Objects;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorUnitConfigurationProducerTest extends BasePlatformTestCase {

    @Rule
    private final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/aldorgit/opt");

    @Override
    public void setUp() throws Exception {
        JUnits.setLogToDebug();
        super.setUp();
        Assume.assumeTrue(directory.isPresent());
    }

    public void testRunSimpleConfiguration() {
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
        dataContext.put("module", getModule());
        dataContext.put("Location", new PsiLocation<>(elt));
        dataContext.put("project", getProject());

        ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
        System.out.println("Context: " + runContext.getLocation() + " " + runContext.getConfiguration());
        Assert.assertNotNull(runContext.getConfiguration());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

        Assert.assertEquals("FooTest (foo.as)", runnerAndConfigurationSettings.getName());

        RunConfiguration config = runContext.getConfiguration().getConfiguration();
        Assert.assertTrue(config instanceof AldorUnitConfiguration);
        AldorUnitConfiguration aldorConfig = (AldorUnitConfiguration) config;
        Assert.assertEquals(getSourceRoot().getPath() + "/foo.as", aldorConfig.bean().inputFile);
        Assert.assertEquals("FooTest", aldorConfig.bean().typeName);
        Assert.assertEquals("aldor.test", aldorConfig.bean().packageName);
    }

    public void testStartOutside() {
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
        dataContext.put("module", getModule());
        dataContext.put("Location", new PsiLocation<>(elt));
        dataContext.put("project", getProject());
        ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        Assert.assertNull(context.getConfigurationsFromContext());
    }

    private void executeRunner(RunnerAndConfigurationSettings settings) throws ExecutionException {
        Assert.assertTrue(ExecutionTargetManager.canRun(settings.getConfiguration(), ExecutionTargetManager.getActiveTarget(getProject())));
        RunConfiguration runConfiguration = settings.getConfiguration();

        JUnits.ProcessOutput output = JUnits.doStartTestsProcess(runConfiguration);

        LOG.info("Output: " + output.out);
        LOG.info("Error: " + output.err);
        LOG.info("Sys: " + output.sys);
    }

    public void testRun() throws ExecutionException {
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "#include \"aldor.as\"\nFooTest: with == add\n");

        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);
        Assert.assertNotNull(whole);
        PsiElement elt = PsiTreeUtil.findChildOfType(whole, AldorWith.class);
        Assert.assertNotNull(elt);

        MyMapDataContext dataContext = new MyMapDataContext();
        dataContext.put("module", getModule());
        dataContext.put("Location", new PsiLocation<>(elt));
        dataContext.put("project", getProject());

        ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
        Assert.assertNotNull(runContext.getConfiguration());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

        ExecutionTargetManager.canRun(runnerAndConfigurationSettings.getConfiguration(), ExecutionTargetManager.getActiveTarget(getProject()));
        Assert.assertTrue(runnerAndConfigurationSettings.getName().contains("foo"));

        RunConfiguration runConfiguration = runnerAndConfigurationSettings.getConfiguration();
        Assert.assertNotNull(runConfiguration.getConfigurationEditor());

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        ProgramRunner<?> runner = ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, runConfiguration);
        Assert.assertNotNull(runner);
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executor, runner, runnerAndConfigurationSettings, getProject());

        RunContentDescriptor[] descriptorBox = new RunContentDescriptor[1];
        try {
            runner.execute(executionEnvironment, descriptor -> {
                descriptorBox[0] = descriptor;
                LOG.info("Started process - " + descriptor.getDisplayName());
            });
        }
        finally {
            Editor[] editors = EditorFactory.getInstance().getAllEditors();
            for (Editor editor : editors) {
                EditorFactory.getInstance().releaseEditor(editor);
            }
        }
        Objects.requireNonNull(descriptorBox[0].getProcessHandler()).waitFor();
        descriptorBox[0].getExecutionConsole().dispose();
        executionEnvironment.dispose();
        descriptorBox[0].dispose();
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptorWithAldorUnit(directory.path());
    }
}