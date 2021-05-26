package aldor.build.module.editor;

import aldor.build.module.AldorModuleExtension;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.FormHelper;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.ui.FieldPanel;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class AldorBuildElementsFormTest  extends AssumptionAware.LightIdeaTestCase {
    private JUnits.TearDownItem tearDown = new JUnits.TearDownItem();

    @Override
    protected boolean shouldRunTest() {
        return false; // Aldor Module properties are broken
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tearDown = tearDown.with(JUnits.setLogToDebug());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            tearDown.tearDown();
        }
        finally {
            super.tearDown();
        }
    }

    public void testForm() throws ConfigurationException {
        AldorModuleExtension extension = ModuleRootManager.getInstance(getModule()).getModuleExtension(AldorModuleExtension.class);
        AldorBuildElementsForm form = new AldorBuildElementsForm(getModule(), extension.getModifiableModel(true), () -> {});
        FieldPanel outputDirectory = FormHelper.component(form, FieldPanel.class, "outputDirectoryPanel");

        Assert.assertFalse(form.isModified());

        outputDirectory.setText("/foo");

        Assert.assertNotNull(extension.getState());
        Assert.assertNotEquals("/foo", extension.state()._outputDirectory());
        Assert.assertTrue(form.isModified());
        form.apply();
        System.out.println("state " + extension.state()._outputDirectory() + " form " + outputDirectory.getText());
        Assert.assertFalse(form.isModified());
        Assert.assertEquals("/foo", extension.state()._outputDirectory());
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }

}