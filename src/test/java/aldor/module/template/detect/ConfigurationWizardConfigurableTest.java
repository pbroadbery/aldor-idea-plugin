package aldor.module.template.detect;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.test_util.AssumptionAware;
import aldor.test_util.Swings;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import java.io.File;

public class ConfigurationWizardConfigurableTest extends AssumptionAware.BasePlatformTestCase {

    @Override
    protected boolean runInDispatchThread() {
        return true;
    }

    @Test
    public void test() {
        DetectedRootFacetSettings settings = new DetectedRootFacetSettings();
        ConfigurationWizardConfigurable configurable = new ConfigurationWizardConfigurable();
        Assert.assertTrue(configurable.currentState().isEmpty());

        JComponent component = configurable.getComponent();
        Assert.assertNotNull(component);
        Assert.assertTrue(configurable.validate());
        configurable.disposeUIResources();
    }

    @Test
    public void testOneItem() {
        DetectedRootFacetSettings settings = new DetectedRootFacetSettings();
        settings.put(new File("/tmp"), ConfigRootFacetProperties.newBuilder().build());
        ConfigurationWizardConfigurable configurable = new ConfigurationWizardConfigurable();
        Assert.assertTrue(configurable.currentState().isEmpty());
        Assert.assertTrue(configurable.validate());

        configurable.initialise(settings);
        Assert.assertFalse(configurable.currentState().isEmpty());
        Assert.assertTrue(configurable.validate());

        JComponent component = configurable.getComponent();
        Assert.assertNotNull(component);
        Assert.assertTrue(configurable.validate());

        var pane = (JTabbedPane) component;
        Assert.assertEquals(1, pane.getTabCount());
        var textField = Swings.findChildComponent(pane, Swings.byName("Build"), ExtendableTextField.class).orElseThrow();
        textField.setText("/tmp/build");

        var newState = configurable.currentState();
        Assert.assertEquals("/tmp/build", newState.get(new File("/tmp")).buildDirectory());
        configurable.disposeUIResources();
    }



}