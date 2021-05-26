package aldor.builder.jps;

import aldor.builder.jps.module.AldorFacetExtensionProperties;
import aldor.builder.jps.module.MakeConvention;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JpsAldorModulePropertiesTest {

    @Test
    public void testIsValid_whenValid() {
        AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Disabled, "java-sdk", MakeConvention.Source, "", "");
        assertTrue(properties.isValid());
    }

    @Test
    public void testJavaComponents() {
        AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
        assertTrue(properties.buildJavaComponents());
    }
}