package aldor.jps;

import com.intellij.openapi.diagnostic.Logger;
import org.junit.Assert;

import java.io.File;

public class JpsAldorInstalledBuildTest extends AldorJpsTestCase {
    private static final Logger LOG = Logger.getInstance(JpsAldorInstalledBuildTest.class);
    private final AldorInstalledFixture aldorFixture = new AldorInstalledFixture();

    public void testOne() {

        aldorFixture.createModule();
        createFile("foo.as", "X: with == add");

        createFile("Makefile", """
                out/ao/foo.ao: foo.as
                --TAB--> mkdir -p $(dir $@)
                --TAB--> echo compiling foo.as
                --TAB--> touch $@
                out/jar/prj.jar: out/ao/foo.ao
                --TAB--> mkdir -p $(dir $@)
                --TAB--> touch $@
                """);

        Assert.assertTrue(fileForProjectPath("foo.as").exists());

        LOG.info("RebuildAll STARTS");
        BuildResult result = rebuildAllAndSucceed();
        LOG.info("Mappings: " + result.getMappingsDump());
        LOG.info("RebuildAll ENDS");
        Assert.assertTrue(new File(getOrCreateProjectDir() + "/out/ao/foo.ao").exists());
    }

}
