package aldor.runconfiguration.spad;

import aldor.psi.AldorLiteral;
import aldor.runconfiguration.MyMapDataContext;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.SourceFileStorageType;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Objects;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class SpadInputRunConfigurationProducerTest {
    public final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu");
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor());

    @Rule
    public final TestRule rule =
            RuleChain.emptyRuleChain()
                    .around(directory)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testCreateInputFile() throws ExecutionException {
        JUnits.setLogToInfo();
        VirtualFile file = createFile(getSourceRoot(), "foo.input", "23\n)quit\n");

        PsiFile whole = PsiManager.getInstance(codeTestFixture.getProject()).findFile(file);
        Assert.assertNotNull(whole);
        AldorLiteral theNumber = Objects.requireNonNull(PsiTreeUtil.findChildOfType(whole, AldorLiteral.class));
        MyMapDataContext dataContext = new MyMapDataContext();
        dataContext.put("module", codeTestFixture.getModule());
        dataContext.put("Location", new PsiLocation<>(theNumber));
        dataContext.put("project", codeTestFixture.getProject());

        ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
        System.out.println("Context: " + runContext.getLocation() + " " + runContext.getConfiguration());
        Assert.assertNotNull(runContext.getConfiguration());
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

        ExecutionTargetManager.canRun(runnerAndConfigurationSettings.getConfiguration(), ExecutionTargetManager.getActiveTarget(codeTestFixture.getProject()));
        Assert.assertTrue(runnerAndConfigurationSettings.getName().contains("foo"));

        RunConfiguration runConfiguration = runnerAndConfigurationSettings.getConfiguration();
        Assert.assertNotNull(runConfiguration.getConfigurationEditor());

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        ProgramRunner<?> runner = ProgramRunner.getRunner(DefaultRunExecutor.EXECUTOR_ID, runConfiguration);
        Assert.assertNotNull(runner);
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executor, runner, runnerAndConfigurationSettings, codeTestFixture.getProject());

        RunContentDescriptor[] descriptorBox = new RunContentDescriptor[1];

        //noinspection UnstableApiUsage
        executionEnvironment.setCallback(new ProgramRunner.Callback() {
                                        @Override
                                        public void processStarted(RunContentDescriptor descriptor) {
                                            System.out.println("Running: " + descriptor);
                                            descriptorBox[0] = descriptor;
                                        }});

        runner.execute(executionEnvironment);
        Assert.assertNotNull(descriptorBox[0]);
        RunContentDescriptor descriptor = descriptorBox[0];
        Assert.assertNotNull(descriptor.getProcessHandler());
        descriptor.getProcessHandler().waitFor();

        descriptorBox[0].getExecutionConsole().dispose();
        executionEnvironment.dispose();
        descriptorBox[0].dispose();
    }

    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(directory, SourceFileStorageType.Real);
    }

}
