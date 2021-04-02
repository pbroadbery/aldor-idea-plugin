package aldor.module.template;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetConstants;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.build.module.AldorModuleBuilder;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.intellij.facet.FacetManager;
import com.intellij.ide.projectWizard.NewProjectWizardTestCase;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.Consumer;
import org.junit.Assert;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Objects;

@Ignore("Does need to work, but not at the moment")
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
            public void consume(Step step) {
                LOG.info("Adjusting " + step + "  " + step.getClass());
                adjust(step);
                LOG.info("Adjusted " + step + "  " + step.getClass());
            }
        });

        FacetManager facetManager = FacetManager.getInstance(ModuleManager.getInstance(project).getModules()[0]);
        AldorFacet facet = facetManager.findFacet(AldorFacetType.instance().getId(), AldorFacetConstants.NAME);

        LOG.info("Facet " + facet);
        Assert.assertNotNull(facet);

        Assert.assertEquals("aldor sdk", Objects.requireNonNull(facet.getConfiguration().getState()).sdkName());
    }

    private void adjust(Step step) {
        AldorSimpleModuleBuilder builder = (AldorSimpleModuleBuilder) myWizard.getProjectBuilder();
        if (step instanceof AldorNewModuleFacetStep) {
            LOG.info("Setting SDK: " + aldorSdk.getName());
            AldorNewModuleFacetStep facetStep = (AldorNewModuleFacetStep) step;
            facetStep.updateSdk(aldorSdk.getName());
        }
    }

    public void testMissingAldorSdk() {
        try {
            Project project = createProjectFromTemplate("Aldor", "Simple Aldor module", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
