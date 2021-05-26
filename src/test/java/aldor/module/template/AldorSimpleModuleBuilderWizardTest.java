package aldor.module.template;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetConstants;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.intellij.facet.FacetManager;
import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AldorSimpleModuleBuilderWizardTest extends AssumptionAware.NewProjectWizardTestCase {
    private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilderWizardTest.class);
    private Sdk aldorSdk;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createSdk("foo", JavaSdk.getInstance());
        aldorSdk = createSdk("aldor sdk", AldorInstalledSdkType.instance());
        JUnits.setLogToInfo();
    }

    public void testWizardSettings() throws IOException {
        Project project = createProjectFromTemplate("Aldor", "Simple Aldor module", new Consumer<Step>() {
            @Override
            public void accept(Step step) {
                LOG.info("Adjusting " + step + "  " + step.getClass());
                if (step instanceof ProjectSettingsStep) {
                    ProjectSettingsStep projectSettingsStep = (ProjectSettingsStep) step;
                    @Nullable ModuleWizardStep sstep = projectSettingsStep.getSettingsStep();
                    LOG.info("Wizard Step: " + sstep.getName() + "  " + sstep.getClass());
                    AldorSimpleModuleBuilder builder = (AldorSimpleModuleBuilder) myWizard.getProjectBuilder();
                    builder.setSdk(aldorSdk);
                }
                LOG.info("Adjusted " + step + "  " + step.getClass());
            }
        });
        LOG.info("Created project");

        Module module = ModuleManager.getInstance(project).getModules()[0];
        FacetManager facetManager = FacetManager.getInstance(module);
        AldorFacet facet = facetManager.findFacet(AldorFacetType.instance().getId(), AldorFacetConstants.NAME);

        LOG.info("Facet " + facet);
        Assert.assertNotNull(facet);

        Assert.assertEquals("aldor sdk", Objects.requireNonNull(facet.getConfiguration().getState()).sdkName());
        Assert.assertEquals("out", facet.getConfiguration().getState().relativeOutputDirectory());
        VirtualFile root = ModuleRootManager.getInstance(module).getContentRoots()[0];
        Assert.assertNotNull(root);
        VirtualFile srcDir = Optional.ofNullable(root.findChild("src")).orElseThrow();
        Assert.assertTrue(srcDir.isDirectory());
        Assert.assertEquals(srcDir, ModuleRootManager.getInstance(module).getSourceRoots()[0]);
        VirtualFile makefile = Optional.ofNullable(srcDir.findChild("Makefile")).orElseThrow();
        Assert.assertTrue(makefile.exists());

        String text = VfsUtil.loadText(makefile);
        System.out.println("Text:\n" + text);
        Assert.assertTrue("module '" + module.getName()+ "' ", text.contains("module '" + module.getName() + "' "));
    }

    public void testMissingAldorSdk() {
        try {
            Project project = createProjectFromTemplate("Aldor", "Simple Aldor module", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
