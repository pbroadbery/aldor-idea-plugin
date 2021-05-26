package aldor.builder.jps;

import aldor.builder.jps.module.AldorFacetExtensionProperties;
import aldor.builder.jps.module.AldorModuleFacade;
import aldor.builder.jps.module.AldorModuleState;
import aldor.builder.jps.module.MakeConvention;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class JpsAldorModuleTypeTest {

    @Test
    public void testSourceCase_Local_is_in_source() {
        for (String outputDir: new String[] {".", "./"}) {
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().outputDirectory(outputDir).makeConvention(MakeConvention.Source).build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().makeConvention(MakeConvention.Source).relativeOutputDirectory(outputDir).build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject");
            File sourceFile = new File("/tmp/myproject/foo.as");
            File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
            String tgt = aldor.targetName(sourceRoot, sourceFile);
            assertEquals("/tmp/myproject", dir.toString());
            assertEquals("out/ao/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Source_IsUsed() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().makeConvention(MakeConvention.Source).relativeOutputDirectory("out/ao").build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().makeConvention(MakeConvention.Source).outputDirectory(outputDir).relativeOutputDirectory("out/ao").build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().outputDirectory(outputDir).makeConvention(MakeConvention.Build).build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk",
                    AldorFacetExtensionProperties.WithJava.Enabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().makeConvention(MakeConvention.Build).outputDirectory(outputDir).build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
            AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Disabled, "java-sdk", MakeConvention.Source, "", "");
            AldorModuleState moduleState = AldorModuleState.newBuilder().makeConvention(MakeConvention.Build).outputDirectory(outputDir).build();
            AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
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
        AldorFacetExtensionProperties properties = new AldorFacetExtensionProperties("aldor-sdk", AldorFacetExtensionProperties.WithJava.Disabled, "java-sdk", MakeConvention.Source, "", "");
        AldorModuleState moduleState = AldorModuleState.newBuilder().build();
        AldorModuleFacade aldor = new AldorModuleFacade(moduleState, properties);
        File contentRoot = new File("/tmp/myproject");
        File sourceRoot = new File("/tmp/myproject");
        File sourceFile = new File("/tmp/myproject/foo.as");
        File dir = aldor.buildDirectory(contentRoot, sourceRoot, sourceFile);
        String tgt = aldor.targetName(sourceRoot, sourceFile);
        assertEquals("/tmp/myproject", dir.toString());
        assertEquals("foo.ao", tgt);
    }
}