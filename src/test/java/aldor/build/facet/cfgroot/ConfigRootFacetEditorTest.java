package aldor.build.facet.cfgroot;

import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.ProjectFacetsConfigurator;
import com.intellij.facet.impl.ui.FacetEditorImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import org.junit.Assert;


public class ConfigRootFacetEditorTest extends AssumptionAware.LightIdeaTestCase {
    private ConfigRootFacet facet = null;

    @Override
    protected void setUp() throws Exception {
        withSafeTearDown(() -> System.out.println("Goodbye 1"));
        super.setUp();
        withSafeTearDown(JUnits.setLogToDebug());
        withSafeTearDown(() -> System.out.println("Goodbye 2"));

        createFacet();
    }

    private void createFacet() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            ConfigRootFacetConfiguration configuration = new ConfigRootFacetConfiguration();
            Module module = getModule();
            this.facet = FacetManager.getInstance(module).createFacet(ConfigRootFacetType.instance(), "rootFacet", configuration, null);
        });
    }

    private void destroyFacet() {
        if (facet != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                    var model = FacetManager.getInstance(getModule()).createModifiableModel();
                    model.removeFacet(facet);
                    model.commit();
            });
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            destroyFacet();
        }
        finally {
            super.tearDown();
        }
    }

    public void testEditor() {
        ModulesConfigurator configurator = new ModulesConfigurator(getProject(), ProjectStructureConfigurable.getInstance(getProject()));
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        configurator.getOrCreateModuleEditor(getModule());
        Facet<?> facet = facetConfigurator.createAndAddFacet(getModule(), ConfigRootFacetType.instance(), null);
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        ConfigRootFacetEditor tab = editor.getEditorTab(ConfigRootFacetEditor.class);

        System.out.println(tab.currentState());
        //noinspection OverlyStrongTypeCast
        System.out.println(((ConfigRootFacetConfiguration) facet.getConfiguration()).getState());
        Assert.assertFalse(tab.isModified());

        editor.disposeUIResources();
    }
}