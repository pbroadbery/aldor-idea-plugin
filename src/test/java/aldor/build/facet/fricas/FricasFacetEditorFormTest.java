package aldor.build.facet.fricas;

import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.facet.impl.ProjectFacetsConfigurator;
import com.intellij.facet.impl.ui.FacetEditorImpl;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

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
        ModulesConfigurator configurator = new ModulesConfigurator(getProject(), ProjectStructureConfigurable.getInstance(getProject()));
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
