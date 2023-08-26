package aldor.build.facet.cfgroot;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.test_util.AssumptionAware;
import aldor.test_util.Swings;
import org.junit.Assert;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class ConfigRootConfigurableTest extends AssumptionAware.BasePlatformTestCase {

    public void test() {
        ConfigRootFacetProperties properties = ConfigRootFacetProperties.newBuilder().build();
        ConfigRootConfigurable configRootConfigurable = new ConfigRootConfigurable(properties);
        JComponent component = configRootConfigurable.createComponent();

        Assert.assertFalse(configRootConfigurable.isModified());

        var txt = Swings.findChildComponent(component, x -> true, JTextField.class).get();
        txt.setText("hello");
        Assert.assertTrue(configRootConfigurable.isModified());
    }
}