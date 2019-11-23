package aldor.expression;

import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;
import org.junit.Assume;

import java.util.Collection;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class ExpressionFileTest extends BasePlatformTestCase {

    public void testIndexing() {
        Project project = getProject();

        VirtualFile file = createFile(getSourceRoot(), "foo2.expr", "a == b; c == d");
        Assert.assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        Assert.assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);

        Collection<String> ll = ExpressionDefineStubIndex.instance.getAllKeys(project);
        Assert.assertEquals(2, ll.size());
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        ExecutablePresentRule.Aldor aldorRule = new ExecutablePresentRule.Aldor();
        Assume.assumeTrue(aldorRule.shouldRunTest());
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorRule.prefix());
    }

}

