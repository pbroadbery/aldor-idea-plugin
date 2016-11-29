package aldor.builder;

import aldor.builder.test.AldorJpsTestCase;
import aldor.builder.test.BuildResult;
import aldor.builder.test.CompileScopeTestBuilder;
import com.google.common.io.Files;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.model.module.JpsModule;
import org.junit.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JpsAldorBuildTest extends AldorJpsTestCase {
    private static final Logger LOG = Logger.getInstance(JpsAldorBuildTest.class);
    private AldorFixture aldorFixture = new AldorFixture(this);

    public void testOne() throws IOException {

        aldorFixture.createModule();
        createFile("aldor-codebase/foo.as", "X: with == add");
        createFile("aldor-codebase/bar.as", "X: with == add");
        createFile("aldor-codebase/configure.ac", "DUMMY_TEXT");

        createFile("aldor-codebase/build/Makefile", "foo.abn: ../foo.as\nbar.abn: ../bar.as\nfoo.abn bar.abn: %:\n\techo build $@\n\tcat $< > $@");

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndSucceed();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("RebuildAll ENDS");

        Assert.assertTrue(fileForProjectPath("aldor-codebase/foo.as").exists());
        Assert.assertTrue(fileForProjectPath("aldor-codebase/build/Makefile").exists());

        Assert.assertTrue(Files.toString(fileForProjectPath("aldor-codebase/build/bar.abn"),
                                                StandardCharsets.US_ASCII).contains("X: with"));

        change("aldor-codebase/bar.as", "Y: with == add");

        result = doBuild(CompileScopeTestBuilder.make().all()).assertSuccessful();
        LOG.info("Mappings: " + result.getMappingsDump());
        Assert.assertTrue(Files.toString(fileForProjectPath("aldor-codebase/bar.as"),
                                            StandardCharsets.US_ASCII).contains("Y: with"));

        Assert.assertTrue(Files.toString(fileForProjectPath("aldor-codebase/build/bar.abn"),
                                            StandardCharsets.US_ASCII).contains("Y: with"));
    }

    public void testErrors() throws IOException {
        aldorFixture.createModule();

        createFile("aldor-codebase/foo.as", "X: with == add");
        createFile("aldor-codebase/error.txt", "\"../foo.as\", line 1: \n" +
                "foo\n" +
                "^\n" +
                "[L1 C1] #1 (Error) No meaning for identifier `foo'.\n");
        createFile("aldor-codebase/configure.ac", "DUMMY_TEXT");

        createFile("aldor-codebase/build/Makefile", "foo.abn: ../foo.as\n\tcat ../error.txt\n\tcat $< > $@\n\tfalse\n");

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndFail();
        LOG.info("RebuildAll ENDS");

        LOG.info("Info messages: " + result.getMessages(BuildMessage.Kind.INFO));
        LOG.info("Error messages: " + result.getMessages(BuildMessage.Kind.ERROR));

    }

    class AldorFixture {
        String projectName = "aldor-codebase";
        AldorJpsTestCase testCase;

        AldorFixture(AldorJpsTestCase testCase) {
            this.testCase = testCase;
        }

        public void createModule() {
            JpsModule module = addModule(projectName);
            Assert.assertNotNull(module);
        }


    }

}
