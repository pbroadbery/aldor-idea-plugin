package aldor.references;

import aldor.psi.AldorId;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.VirtualFileTests;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;

import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorLibraryRefTest extends BasePlatformTestCase {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Override
    protected void setUp() throws Exception {
        JUnits.setLogToDebug();
        super.setUp();
    }

    public void testReference() {
        for (Sdk allJdk : ProjectJdkTable.getInstance().getAllJdks()) {
            System.out.println("JDK: " + allJdk);
        }

        ProjectFileIndex.getInstance(getProject()).iterateContent(fileOrDir -> {
            LOG.info("Found file " + fileOrDir.getCanonicalPath());
            return true;
        });
        ModuleRootManager.getInstance(getModule()).getFileIndex().iterateContent(f -> {LOG.info("Found module file " + f.getCanonicalPath()); return true;});

        String text = "Foo: Category == Ring with\n";
        VirtualFile file = createAldorFile(text);
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, getProject(), null);
        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);

        PsiElement theId = PsiTreeUtil.findChildrenOfType(whole, AldorId.class).stream().filter(id -> "Ring".equals(id.getText())).findFirst().orElse(null);
        Assert.assertNotNull(theId);
        PsiReference[] refs = theId.getReferences();
        PsiReference ref = refs[0];
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals("sit_ring.as", resolved.getContainingFile().getName());
    }


    private VirtualFile createAldorFile(String text) {
        return VirtualFileTests.createFile(getSourceRoot(), "foo.as", text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        Assume.assumeTrue(aldorExecutableRule.shouldRunTest());
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix());
    }
}
