package aldor.references;

import aldor.psi.AldorId;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.VirtualFileTests;
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
import org.junit.Assert;

import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class SpadLibraryRefTest extends AssumptionAware.BasePlatformTestCase {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();

    public void testReference() {
        JUnits.setLogToDebug();
        String text = ")abbrev domain FOO Foo\nFoo: Category == Ring with\n";
        VirtualFile file = createSpadFile(text);
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);

        ProjectFileIndex.getInstance(getProject()).iterateContent(fileOrDir -> {
            LOG.info("Found file " + fileOrDir.getCanonicalPath());
            return true;
        });
        ModuleRootManager.getInstance(getModule()).getFileIndex().iterateContent(f -> {LOG.info("Found module file " + f.getCanonicalPath()); return true;});

        PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);

        PsiElement theId = PsiTreeUtil.findChildrenOfType(whole, AldorId.class).stream().filter(id -> "Ring".equals(id.getText())).findFirst().orElse(null);
        Assert.assertNotNull(theId);
        PsiReference[] refs = theId.getReferences();
        PsiReference ref = refs[0];
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals("catdef.spad", resolved.getContainingFile().getName());
    }


    private VirtualFile createSpadFile(String text) {
        return VirtualFileTests.createFile(getSourceRoot(), "foo.spad", text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule);
    }

    }
