package aldor.editor.completion;

import aldor.build.facet.fricas.FricasFacet;
import aldor.sdk.SdkTypes;
import aldor.spad.FricasSpadLibraryBuilder;
import aldor.spad.SpadLibrary;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.Timer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;

import java.util.List;

public class AldorCompletionContributorTest extends AssumptionAware.LightIdeaTestCase {

    private final ExecutablePresentRule directory = new ExecutablePresentRule.Fricas();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(directory.shouldRunTest());
    }

    @Override
    protected boolean runInDispatchThread() {
        return true;
    }

    public void testLoadAllBenchmark() {
        VirtualFile algebraDirectory = projectSdkAlgebraDirectory();
        SpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(getProject())
                .daaseDirectory(algebraDirectory)
                .createFricasSpadLibrary();
        ProgressManager.getInstance().executeNonCancelableSection(() -> {
            for(int i = 0; i<1;i++) {
                Timer timer = new Timer("loadAllTypes-" + i);
                try (Timer.TimerRun run = timer.run()) {
                    AldorCompletionContributor.allTypes_withCancel(lib);
                } catch (Exception ignore) {
                    Assert.fail();
                }
                System.out.println("Read files: " + timer);
            }
        });
    }

    public void testLoadAll() {
        SpadLibrary lib = new FricasSpadLibraryBuilder().project(getProject())
                .daaseDirectory(projectSdkAlgebraDirectory())
                .createFricasSpadLibrary();

        ProgressManager.getInstance().executeNonCancelableSection(() -> {
            List<LookupElement> allTypes = AldorCompletionContributor.allTypes_withCancel(lib);
            System.out.println("All types: " + allTypes.size());
            Assert.assertFalse(allTypes.isEmpty());
        });
    }

    @NotNull
    private VirtualFile projectSdkAlgebraDirectory() {
        Sdk projectSdk = FricasFacet.forModule(getModule()).getConfiguration().sdk();
        Assert.assertNotNull(projectSdk);
        VirtualFile algebraDirectory = SdkTypes.algebraPath(projectSdk);
        Assert.assertNotNull(algebraDirectory);
        return algebraDirectory;
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(directory);
    }

}
