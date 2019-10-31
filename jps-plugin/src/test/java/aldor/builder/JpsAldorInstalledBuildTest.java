package aldor.builder;

import aldor.builder.test.AldorJpsTestCase;
import aldor.builder.test.BuildResult;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

public class JpsAldorInstalledBuildTest extends AldorJpsTestCase {
    private static final Logger LOG = Logger.getInstance(JpsAldorInstalledBuildTest.class);
    private final AldorInstalledFixture aldorFixture = new AldorInstalledFixture();

    public void testOne() throws IOException {

        aldorFixture.createModule();
        createFile("foo.as", "X: with == add");

        createFile("Makefile", "out/ao/foo.ao: foo.as\n"+
                "\tmkdir -p $(dir $@)\n" +
                "\techo compiling foo.as\n" +
                "\ttouch $@\n" +
                "out/jar/prj.jar: out/ao/foo.ao\n" +
                "\tmkdir -p $(dir $@)" +
                "\ttouch $@\n");

        Assert.assertTrue(fileForProjectPath("foo.as").exists());

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndSucceed();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("RebuildAll ENDS");
        Assert.assertTrue(new File(getOrCreateProjectDir() + "/out/ao/foo.ao").exists());
    }

}
