package aldor.builder.jps;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class JpsAldorModuleTypeTest {

    @Test
    public void testSourceCase_Local_is_in_source() {
        for (String outputDir: new String[] {".", "./"}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject");
            File sourceFile = new File("/tmp/myproject/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Local_SubDir_is_SubDir() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject");
            File sourceFile = new File("/tmp/myproject/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject", dir.toString());
            assertEquals("out/ao/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Source_IsUsed() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/source", dir.toString());
            assertEquals("out/ao/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_Source_IsUsed_Subdir() {
        for (String outputDir: new String[] {"out/ao", "./out/ao", "./out/ao/.", "out/ao/."}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/bar/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/source", dir.toString());
            assertEquals("out/ao/bar/foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_NonLocal_SubDir_is_NonLocal() {
        for (String outputDir: new String[] {"/tmp/myproject/wibble", "/tmp/myproject/wibble/."}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/source", dir.toString());
            assertEquals("/tmp/myproject/wibble/foo.ao", tgt);
        }
    }

    @Test
    public void testBuildCase() {
        for (String outputDir: new String[] {"/tmp/myproject/build"}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.BuildRelative, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/build", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testBuildCase_subdir() {
        for (String outputDir: new String[] {"/tmp/myproject/build"}) {
            AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", outputDir, JpsAldorMakeDirectoryOption.BuildRelative, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
            File contentRoot = new File("/tmp/myproject");
            File sourceRoot = new File("/tmp/myproject/source");
            File sourceFile = new File("/tmp/myproject/source/wibble/foo.as");
            File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
            String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
            assertEquals("/tmp/myproject/build/wibble", dir.toString());
            assertEquals("foo.ao", tgt);
        }
    }

    @Test
    public void testSourceCase_missingOutDir() {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", "", JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Disabled, "java-sdk");
        File contentRoot = new File("/tmp/myproject");
        File sourceRoot = new File("/tmp/myproject");
        File sourceFile = new File("/tmp/myproject/foo.as");
        File dir = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot, sourceFile);
        String tgt = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot, sourceFile);
        assertEquals("/tmp/myproject", dir.toString());
        assertEquals("out/ao/foo.ao", tgt);
    }
}