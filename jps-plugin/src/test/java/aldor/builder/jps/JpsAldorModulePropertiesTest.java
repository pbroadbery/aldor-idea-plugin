package aldor.builder.jps;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JpsAldorModulePropertiesTest {

    @Test
    public void testIsValid_whenInvalid() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", "/", JpsAldorMakeDirectoryOption.Invalid, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
        assertFalse(properties.isValid());
    }

    @Test
    public void testIsValid_whenValid() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", "/", JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
        assertTrue(properties.isValid());
    }

    @Test
    public void testIsValid_whenMissingDir() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", "", JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
        assertFalse(properties.isValid());
    }

    @Test
    public void testIsValid_whenNullDir() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", null, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
        assertFalse(properties.isValid());
    }

    @Test
    public void testJavaComponents() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", null, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
        assertTrue(properties.buildJavaComponents());
    }

}