package aldor.build.facet.fricas;

import aldor.build.facet.aldor.AldorFacetConfiguration;
import aldor.build.facet.aldor.AldorFacetEditorForm;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.ProjectFacetsConfigurator;
import com.intellij.facet.impl.ui.FacetEditorImpl;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import static org.junit.Assert.*;

public class FricasFacetEditorFormTest extends AssumptionAware.LightIdeaTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testForm() {
        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        configurator.getOrCreateModuleEditor(getModule());
        //Facet<?> facet = facetConfigurator.createAndAddFacet(getModule(), FricasFacetType.instance(), null);
        FricasFacet facet = facetConfigurator.getFacetsByType(getModule(), FricasFacetType.instance().getId()).stream().findFirst().orElse(null);
        Assert.assertNotNull(facet);
        Assert.assertNotNull(facet.getConfiguration());
        Assert.assertNotNull(facet.getConfiguration().sdk());
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        FricasFacetEditorForm tab = editor.getEditorTab(FricasFacetEditorForm.class);

        Assert.assertEquals(tab.currentState().sdkName(), facet.getConfiguration().sdk().getName());

        editor.disposeUIResources();
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(ExecutablePresentRule.Fricas.INSTANCE);
    }
}
