package aldor.builder.maketarget;

import aldor.helpers.CompileScopeTestBuilder;
import aldor.helpers.JpsBuilderFixture;
import aldor.util.AssumptionAware;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;

public class GitRepoRoundTripTest extends AssumptionAware.UsefulTestCase {
    private static final Logger LOG = Logger.getInstance(GitRepoRoundTripTest.class);
    private GitRepositoryFixture gitCloneFixture;
    private File mySourceDirectory;
    private JpsBuilderFixture builderFixture;

    public GitRepoRoundTripTest() {
        Logger.setUnitTestMode();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mySourceDirectory = FileUtil.createTempDirectory("compile-server-" + getProjectName(), null);

        gitCloneFixture = new GitRepositoryFixture(mySourceDirectory);
        Assume.assumeTrue(gitCloneFixture.canCreate());
        builderFixture = new JpsBuilderFixture(getProjectName());
        builderFixture.setUp();
    }

    public void testRoundTrip() {
        gitCloneFixture.cloneDirectory();
        var model = gitCloneFixture.getJpsModel();
        Assert.assertNotNull(model);

        //Assert.assertEquals("file:///" + gitCloneFixture.getRootDirectory() + "/aldor", autoconf.directory().getPath());

        builderFixture.setModel(model);

        // FIXME: Build lib/aldor/src/datatypes/array.ao
        CompileScopeTestBuilder scope = CompileScopeTestBuilder.make()
                .targetTypes()
                .all();
        var result = builderFixture.doBuild(scope);
        Assert.assertTrue(result.isSuccessful());
        File configStatus = new File(gitCloneFixture.rootDirectory(), "build/config.status");
        Assert.assertTrue(configStatus.exists());
    }


    protected String getProjectName() {
        return StringUtil.decapitalize(StringUtil.trimStart(getName(), "test"));
    }

}
