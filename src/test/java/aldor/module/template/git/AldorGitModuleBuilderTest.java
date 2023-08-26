package aldor.module.template.git;

import aldor.builder.jps.AldorSourceRootType;
import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.DefaultModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class AldorGitModuleBuilderTest extends AssumptionAware.UsefulTestCase {
    private static final Logger LOG = Logger.getInstance(AldorGitModuleBuilderTest.class);

    private final IdeaProjectTestFixture fixture = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder("simple-project").getFixture();

    @Rule
    public TestRule testRule = RuleChain.emptyRuleChain()
            .around(JUnits.fixtureRule(fixture))
            .around(JUnits.setLogToDebugTestRule)
            ;

    @Test
    public void testBuilder() throws Exception {
        ModifiableModuleModel model = ModuleManager.getInstance(fixture.getProject()).getModifiableModel();
        //model.newModule("/tmp", "Aldor");
        var tmp = Files.createTempDirectory("module-test-");
        try {
            DefaultModulesProvider modulesProvider = new DefaultModulesProvider(fixture.getProject());
            AldorGitModuleBuilder builder = new AldorGitModuleBuilder(GitModuleType.Aldor);
            builder.setContentEntryPath(tmp.toString());
            builder.setName("Git_Aldor");
            builder.setModuleFilePath(tmp.toString());
            ModuleWizardStep[] steps = builder.createFinishingSteps(new WizardContext(fixture.getProject(), fixture.getTestRootDisposable()), modulesProvider);
            for (var step: steps) {
                step.getComponent();
            }

            for (var step: steps) {
                step.updateStep();
            }

            for (var step: steps) {
                step.updateDataModel();
            }
            for (var step: steps) {
                step._commit(true);
            }

            for (var step: steps) {
                step.onWizardFinished();
            }

            builder.commitModule(model.getProject(), null);
            List<VirtualFile> roots = ProjectRootManager.getInstance(fixture.getProject()).getModuleSourceRoots(Set.of(AldorSourceRootType.INSTANCE));
            assertFalse(roots.isEmpty());
            LOG.debug("Roots " + roots);
        }
        finally {
            File file = tmp.toFile();
            FileUtils.deleteDirectory(file);
        }
    }

}