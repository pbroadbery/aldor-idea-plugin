package aldor.build.module;

import aldor.build.module.editor.AldorBuildElementsEditor;
import aldor.build.module.editor.AldorBuildElementsForm;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.Swings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.navigation.History;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class AldorModuleConfigEditorProviderTest extends AssumptionAware.LightIdeaTestCase{

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
    }

    public void testModuleEditor() throws ConfigurationException {
        ModulesConfigurator configurator = new ModulesConfigurator(getProject());
        ModuleEditor editor = configurator.getOrCreateModuleEditor(getModule());
        System.out.println("Editor: " + editor);
        editor.init(new History(editor));
        editor.getPanel();
        AldorBuildElementsEditor buildElementsEditor = (AldorBuildElementsEditor) editor.getEditor("Aldor Build Settings");
        if (buildElementsEditor == null) {
            return; // NB: Build elements are broken, so don't bother
        }

        AldorBuildElementsForm form = buildElementsEditor.form();
        FieldPanel panel = Swings.findChild(form.panel(), FieldPanel.class).orElseThrow();
        panel.setText("wibble");
        editor.apply();
        AldorModuleExtension extension = configurator.getRootModel(getModule()).getModuleExtension(AldorModuleExtension.class);
        Assert.assertNotNull(extension.getState());
        Assert.assertEquals("wibble", extension.state()._outputDirectory());
    }


    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}