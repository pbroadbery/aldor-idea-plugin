package aldor.references;

import aldor.language.SpadLanguage;
import aldor.psi.AldorId;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.junit.Assume;

public class SpadNameReferenceTest extends BasePlatformTestCase {
    private final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(directory.isPresent());
    }

    public void testReference() {
        String text = "Foo(X: Integer): String == never";
        PsiFile file = createSpadFile(text);
        AldorId elt = PsiTreeUtil.findElementOfClassAtOffset(file, text.indexOf("Integer"), AldorId.class, true);
        Assert.assertNotNull(elt);
        PsiReference ref = elt.getReference();
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertTrue(resolved.getContainingFile().getVirtualFile().getPath().contains("integer.spad"));
    }


    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(directory.path());
    }

}
