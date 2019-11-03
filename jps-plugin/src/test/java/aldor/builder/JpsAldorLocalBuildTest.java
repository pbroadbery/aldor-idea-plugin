package aldor.builder;

import aldor.builder.test.AldorJpsTestCase;
import aldor.builder.test.BuildResult;
import aldor.builder.test.CompileScopeTestBuilder;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JpsAldorLocalBuildTest extends AldorJpsTestCase {
    private static final Logger LOG = Logger.getInstance(JpsAldorLocalBuildTest.class);
    private final AldorLocalFixture aldorFixture = new AldorLocalFixture();

    public void testOne() throws IOException {

        aldorFixture.createModule();
        createFile("aldor-codebase/aldor/foo.as", "X: with == add");
        createFile("aldor-codebase/aldor/bar.as", "X: with == add");
        createFile("aldor-codebase/aldor/configure.ac", "DUMMY_TEXT");
        createFile("aldor-codebase/aldor/Makefile.in", "DUMMY_TEXT");

        createFile("aldor-codebase/build/Makefile", "foo.ao: ../aldor/foo.as\nbar.ao: ../aldor/bar.as\nfoo.ao bar.ao: %:\n\techo build $@\n\tcat $< > $@");

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndSucceed();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("RebuildAll ENDS");

        Assert.assertTrue(fileForProjectPath("aldor-codebase/aldor/foo.as").exists());
        Assert.assertTrue(fileForProjectPath("aldor-codebase/aldor/Makefile.in").exists());
        Assert.assertTrue(fileForProjectPath("aldor-codebase/build/Makefile").exists());

        Assert.assertTrue(toString(fileForProjectPath("aldor-codebase/build/bar.ao"),
                                                StandardCharsets.US_ASCII).contains("X: with"));

        change("aldor-codebase/aldor/bar.as", "Y: with == add");

        result = doBuild(CompileScopeTestBuilder.make().all()).assertSuccessful();
        LOG.info("Mappings: " + result.getMappingsDump());
        Assert.assertTrue(toString(fileForProjectPath("aldor-codebase/aldor/bar.as"),
                                            StandardCharsets.US_ASCII).contains("Y: with"));

        Assert.assertTrue(toString(fileForProjectPath("aldor-codebase/build/bar.ao"),
                                            StandardCharsets.US_ASCII).contains("Y: with"));
    }

    public void testTwo() {
        aldorFixture.createModule();

    }

    public void testErrors() throws IOException {
        aldorFixture.createModule();

        createFile("aldor-codebase/aldor/foo.as", "X: with == add");
        createFile("aldor-codebase/aldor/error.txt", "\"../foo.as\", line 1: \n" +
                "foo\n" +
                "^\n" +
                "[L1 C1] #1 (Error) No meaning for identifier `foo'.\n");
        createFile("aldor-codebase/configure.ac", "DUMMY_TEXT");

        createFile("aldor-codebase/build/Makefile", "foo.abn: ../aldor/foo.as\n\tcat ../aldor/error.txt\n\tcat $< > $@\n\tfalse\n");

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndFail();
        LOG.info("RebuildAll ENDS");

        LOG.info("Info messages: " + result.getMessages(BuildMessage.Kind.INFO));
        LOG.info("Error messages: " + result.getMessages(BuildMessage.Kind.ERROR));
        Assert.assertFalse(result.isSuccessful());
    }

    static String toString(File file, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }


}
