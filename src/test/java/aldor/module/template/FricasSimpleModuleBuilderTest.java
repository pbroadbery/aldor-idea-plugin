package aldor.module.template;

import aldor.build.facet.fricas.FricasFacet;
import aldor.sdk.fricas.FricasInstalledSdkType;
import aldor.test_util.JUnits;
import aldor.test_util.Swings;
import com.intellij.ide.projectWizard.NewProjectWizardTestCase;
import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.util.Consumer;
import org.junit.Assert;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Ignore("Causes trouble?")
public class FricasSimpleModuleBuilderTest extends NewProjectWizardTestCase {
    private static final Logger LOG = Logger.getInstance(FricasSimpleModuleBuilderTest.class);
    private JUnits.TearDownItem tearDown = new JUnits.TearDownItem();
    private Sdk fricasSdk;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fricasSdk = createSdk("fricas sdk", FricasInstalledSdkType.instance());
        tearDown = tearDown.with(JUnits.setLogToDebug());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        tearDown.tearDown();
    }

    public void testCreateProject() throws IOException {
        Consumer<Step> consumer = s -> {
            LOG.info("Adjust " + s.getClass().getCanonicalName());
            if (s instanceof ProjectSettingsStep) {
                LOG.info("Setting SDK to " + fricasSdk);
                Optional<JdkComboBox> combo = Swings.findChild(s.getComponent(), JdkComboBox.class);
                Assert.assertTrue(combo.isPresent());
                combo.get().setSelectedJdk(fricasSdk);
                //assertEquals(fricasSdk.getName(), requireNonNull(combo.get().getSelectedJdk()).getName());
            }
        };
        Project project = this.createProjectFromTemplate("FriCAS", "FriCAS Module", consumer);

        Module module = ModuleManager.getInstance(project).getModules()[0];
        FricasFacet facet = FricasFacet.forModule(module);
        Assert.assertNotNull(facet);
        Assert.assertEquals("fricas sdk", requireNonNull(facet.getConfiguration().getState()).sdkName());
    }

    private void adjust(Step step) {
    }


}
