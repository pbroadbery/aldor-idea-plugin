package aldor.builder;

import aldor.builder.test.AldorJpsTestCase;
import aldor.builder.test.BuildResult;
import aldor.builder.test.CompileScopeTestBuilder;
import com.google.common.io.Files;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JpsAldorBuildTest extends AldorJpsTestCase {
    private static final Logger LOG = Logger.getInstance(JpsAldorBuildTest.class);

    public void testOne() {
        JpsModule module = addModule("aldor-codebase", Collections.emptyList(), "out");
        assertNotNull(module);

        LOG.info("Creating files");
        createFile("aldor-codebase/foo.as", "X: with == add");
        createFile("aldor-codebase/bar.as", "X: with == add");
        createFile("aldor-codebase/configure.ac", "DUMMY_TEXT");

        createFile("aldor-codebase/build/Makefile", "foo.abn: ../foo.as\nbar.abn: ../bar.as\nfoo.abn bar.abn: %:\n\techo build $@\n\tcat $< > $@");

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAll();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("RebuildAll ENDS");

        assertTrue(fileForProjectPath("aldor-codebase/foo.as").exists());
        assertTrue(fileForProjectPath("aldor-codebase/build/Makefile").exists());

        assertTrue(fileForProjectPath("aldor-codebase/build/foo.abn").exists());


        change("aldor-codebase/bar.as", "Y: with == add");

        LOG.info("MAKE STARTS");
        result = doBuild(CompileScopeTestBuilder.make().all()).assertSuccessful();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("MAKE ENDS");

        try {
            String text = Files.readLines(fileForProjectPath("aldor-codebase/bar.as"), StandardCharsets.US_ASCII).toString();
            System.out.println("Test result: " + text);
            assertTrue(text.contains("Y: with"));

            text = Files.readLines(fileForProjectPath("aldor-codebase/build/bar.abn"), StandardCharsets.US_ASCII).toString();
            System.out.println("Test result: " + text);
            assertTrue(text.contains("Y: with"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("exception!");
        }
    }

}
