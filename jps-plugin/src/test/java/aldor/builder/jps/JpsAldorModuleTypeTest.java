package aldor.builder.jps;

import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.AldorFacetProperties.WithJava;
import aldor.builder.jps.module.AldorJpsModuleFacade;
import aldor.builder.jps.module.AldorModuleState;
import aldor.builder.jps.module.MakeConvention;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class JpsAldorModuleTypeTest {

    @Test
    public void testSourceCase_Local_is_in_source() {
        for (String outputDir: new String[] {".", "./"}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder()
                    .sdkName("aldor-sdk")
                    .java(WithJava.Enabled)
                    .javaSdkName("java-sdk")
                    .makeConvention(MakeConvention.Source)
                    .outputDirectory("")
                    .relativeOutputDirectory("")
                    .build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject");
            File sourceFile = new File("/tmp/myproject/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Local_SubDir_is_SubDir() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder()
                    .sdkName("aldor-sdk")
                    .java(WithJava.Enabled)
                    .javaSdkName("java-sdk")
                    .makeConvention(MakeConvention.Source)
                    .outputDirectory(outputDir)
                    .relativeOutputDirectory("")
                    .build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject");
            File sourceFile = new File("/tmp/myproject/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Source_IsUsed() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder().java(WithJava.Enabled)
                    .makeConvention(MakeConvention.Source)
                    .relativeOutputDirectory("out/ao").build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/source", dir.toString());
            assertEquals("out/ao/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Source_IsUsed_Subdir() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder().java(WithJava.Enabled).makeConvention(MakeConvention.Source).outputDirectory(outputDir).relativeOutputDirectory("out/ao").build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/bar/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/source", dir.toString());
            assertEquals("out/ao/bar/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_NonLocal_SubDir_is_NonLocal() {
        for (String outputDir: new String[] {"/tmp/myproject/build", "/tmp/myproject/build/."}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder().outputDirectory(outputDir).makeConvention(MakeConvention.Build).java(WithJava.Enabled).build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/build/source", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testBuildCase() {
        for (String outputDir: new String[] {"/tmp/myproject/build"}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder().makeConvention(MakeConvention.Build).outputDirectory(outputDir).java(WithJava.Enabled).build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/build/source", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testBuildCase_subdir() {
        for (String outputDir: new String[] {"/tmp/myproject/build"}) {
            AldorFacetProperties properties = AldorFacetProperties.newBuilder().makeConvention(MakeConvention.Build).outputDirectory(outputDir).java(WithJava.Enabled).build();
            AldorModuleState moduleState = AldorModuleState.newBuilder().build();
            AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/wibble/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/build/source", dir.toString());
            assertEquals("wibble/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_missingOutDir() {
        AldorFacetProperties properties = AldorFacetProperties.newBuilder()
                .sdkName("aldor-sdk")
                .java(WithJava.Disabled)
                .javaSdkName("java-sdk")
                .makeConvention(MakeConvention.Source)
                .outputDirectory("")
                .relativeOutputDirectory("")
                .build();
        AldorModuleState moduleState = AldorModuleState.newBuilder().build();
        AldorJpsModuleFacade aldor = new AldorJpsModuleFacade(moduleState, properties);
        File contentRoot = new File("/tmp/myproject");
        File sourceRoot = new File("/tmp/myproject");
        File sourceFile = new File("/tmp/myproject/foo.as");
        File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
        String tgt = aldor.targetName(sourceRoot, sourceFile);
        assertEquals("/tmp/myproject", dir.toString());
        assertEquals("foo.ao", tgt);
    }
}