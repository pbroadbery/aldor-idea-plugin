package aldor.references;

import aldor.psi.SpadAbbrev;
import aldor.test_util.AssumptionAware;
import aldor.util.VirtualFileTests;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import org.junit.Assert;

import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class SpadAbbrevRefTest extends AssumptionAware.BasePlatformTestCase {

    public void testReference() {
        String text = ")abbrev domain FOO Foo\nFoo: with == add\n";
        VirtualFile file = createSpadFile(text);
        //FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);

        PsiElement theAbbrev = PsiTreeUtil.findChildOfType(whole, SpadAbbrev.class);
        Assert.assertNotNull(theAbbrev);
        PsiReference[] refs = theAbbrev.getReferences();
        PsiReference ref = refs[0];
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("Foo:"), resolved.getTextOffset());
    }


    private VirtualFile createSpadFile(String text) {
        return VirtualFileTests.createFile(getSourceRoot(), "foo.spad", text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }


}
