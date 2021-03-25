package aldor.build.facet.aldor;

import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.FormHelper;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.facet.Facet;
import com.intellij.facet.impl.ProjectFacetsConfigurator;
import com.intellij.facet.impl.ui.FacetEditorImpl;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import javax.swing.JCheckBox;
import java.util.Objects;
import java.util.Optional;

public class AldorFacetEditorFormTest extends LightIdeaTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
    }

    public void testForm() {
        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        configurator.getOrCreateModuleEditor(getModule());
        Facet<?> facet = facetConfigurator.createAndAddFacet(getModule(), AldorFacetType.instance(), null);
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        AldorFacetEditorForm tab = editor.getEditorTab(AldorFacetEditorForm.class);

        System.out.println(tab.currentState());
        //noinspection OverlyStrongTypeCast
        System.out.println(((AldorFacetConfiguration) facet.getConfiguration()).getState());
        Assert.assertFalse(tab.isModified());

        editor.disposeUIResources();
    }

    public void test_change() throws ConfigurationException {
        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        configurator.getOrCreateModuleEditor(getModule());
        AldorFacet facet = (AldorFacet) facetConfigurator.createAndAddFacet(getModule(), AldorFacetType.instance(), null);
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        AldorFacetEditorForm tab = editor.getEditorTab(AldorFacetEditorForm.class);
        JCheckBox checkBox = FormHelper.component(tab, JCheckBox.class, "buildJavaCheckBox");
        checkBox.setSelected(true);
        Assert.assertTrue(tab.isModified());
        tab.apply();
        Assert.assertTrue(Objects.requireNonNull(facet.getConfiguration().getState()).buildJavaComponents());
        editor.disposeUIResources();
    }

    public void test_init() throws ConfigurationException {
        for (Sdk allJdk : ProjectJdkTable.getInstance().getAllJdks()) {
            LOG.info("SDK: "+ allJdk);
        }

        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        AldorFacet facet = AldorFacet.forModule(getModule());
        AldorModuleExtensionProperties state = facet.getConfiguration().getState();
        facet.getConfiguration().updateState(state);
        @NotNull ModuleEditor moduleEditor = configurator.getOrCreateModuleEditor(getModule());
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        AldorFacetEditorForm tab = editor.getEditorTab(AldorFacetEditorForm.class);
        JdkComboBox comboBox = FormHelper.component(tab, JdkComboBox.class, "aldorSdkComboBox");
        LOG.info("Name " + Optional.ofNullable(comboBox.getSelectedJdk()).map(Sdk::getName).orElse(null));
        LOG.info("Mod: " + tab.isModified());
        Assert.assertEquals(facet.getConfiguration().getState().sdkName(), Optional.ofNullable(comboBox.getSelectedJdk()).map(Sdk::getName).orElse(null));
        Assert.assertFalse(tab.isModified());
        LOG.info("Finished!");
    }

    public void test_init_with_invalid_sdk() throws ConfigurationException {
        for (Sdk allJdk : ProjectJdkTable.getInstance().getAllJdks()) {
            LOG.info("SDK: "+ allJdk);
        }

        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        configurator.setContext(new StructureConfigurableContext(getProject(), configurator));
        ProjectFacetsConfigurator facetConfigurator = configurator.getFacetsConfigurator();
        AldorFacet facet = AldorFacet.forModule(getModule());
        AldorModuleExtensionProperties state = facet.getConfiguration().getState();
//        facet.getConfiguration().updateState(state.asBuilder().setSdkName("Not a current SDK").build());
        facet.getConfiguration().updateState(state.asBuilder().setSdkName("nope").build());

        @NotNull ModuleEditor moduleEditor = configurator.getOrCreateModuleEditor(getModule()); // needed to keep next fn happy
        FacetEditorImpl editor = facetConfigurator.getOrCreateEditor(facet);
        AldorFacetEditorForm tab = editor.getEditorTab(AldorFacetEditorForm.class);
        JdkComboBox comboBox = FormHelper.component(tab, JdkComboBox.class, "aldorSdkComboBox");
        LOG.info("Name " + Optional.ofNullable(comboBox.getSelectedJdk()).map(Sdk::getName).orElse(null));
        LOG.info("Mod: " + tab.isModified());
        Assert.assertEquals(null, Optional.ofNullable(comboBox.getSelectedJdk()).map(Sdk::getName).orElse(null));
        Assert.assertTrue(tab.isModified());
        LOG.info("Finished!");
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}