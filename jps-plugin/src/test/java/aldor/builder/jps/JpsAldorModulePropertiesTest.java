package aldor.builder.jps;

import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.MakeConvention;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JpsAldorModulePropertiesTest {

    @Test
    public void testIsValid_whenValid() {
        AldorFacetProperties properties = AldorFacetProperties.newBuilder()
                .sdkName("aldor-sdk")
                .java(AldorFacetProperties.WithJava.Enabled)
                .javaSdkName("java-sdk")
                .makeConvention(MakeConvention.Source)
                .outputDirectory("")
                .relativeOutputDirectory("")
                .build();
        assertTrue(properties.isValid());
    }

    @Test
    public void testJavaComponents() {
        AldorFacetProperties properties = AldorFacetProperties.newBuilder()
                .sdkName("aldor-sdk")
                .java(AldorFacetProperties.WithJava.Enabled)
                .javaSdkName("java-sdk")
                .makeConvention(MakeConvention.Source)
                .outputDirectory("")
                .relativeOutputDirectory("")
                .build();
        assertTrue(properties.buildJavaComponents());
    }
}