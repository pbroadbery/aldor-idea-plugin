package aldor.editor.completion;

import aldor.spad.FricasSpadLibraryBuilder;
import aldor.spad.SpadLibrary;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.Timer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.junit.Assume;

import java.util.List;

public class AldorCompletionContributorTest extends LightPlatformCodeInsightFixtureTestCase {

    private final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(directory.isPresent());
    }

    public void testLoadAllBenchmark() {
        SpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(getProject())
                .daaseDirectory(ProjectRootManager.getInstance(getProject()).getProjectSdk().getHomeDirectory().findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();
        for (int i=0; i<1; i++) {
            Timer timer = new Timer("loadAllTypes-" + i);
            try (Timer.TimerRun run = timer.run()) {
                AldorCompletionContributor.allTypes(lib);
            } catch (Exception ignore) {
                Assert.fail();
            }
            System.out.println("Read files: "+ timer);
        }
    }

    public void testLoadAll() {
        SpadLibrary lib = new FricasSpadLibraryBuilder().project(getProject())
                .daaseDirectory(ProjectRootManager.getInstance(getProject()).getProjectSdk().getHomeDirectory().findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();
        List<LookupElement> allTypes = AldorCompletionContributor.allTypes(lib);
        System.out.println("All types: " + allTypes.size());
        Assert.assertFalse(allTypes.isEmpty());
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(directory.path());
    }

}
