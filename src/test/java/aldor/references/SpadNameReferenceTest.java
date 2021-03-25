package aldor.references;

import aldor.language.SpadLanguage;
import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.test_util.AssumptionAware;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;
import org.junit.Assume;

public class SpadNameReferenceTest extends AssumptionAware.BasePlatformTestCase {
    private final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu");

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

    public void testMacroReference() {
        String text = "Foo(X: Integer): Exp == Impl\n"
                        + " where\n"
                        + "    Exp ==> with\n"
                        + "    Impl ==> add";
        PsiFile file = createSpadFile(text);
        AldorIdentifier implElt = PsiTreeUtil.findElementOfClassAtOffset(file, text.indexOf("Impl\n"), AldorId.class, true);
        Assert.assertNotNull(implElt);
        AldorReference ref = implElt.getReference();
        Assert.assertNotNull(ref);

        PsiElement implResolved = ref.resolveMacro();
        Assert.assertNotNull(implResolved);
        Assert.assertEquals("Impl ==> add", implResolved.getText());

        AldorIdentifier expElt = PsiTreeUtil.findElementOfClassAtOffset(file, text.indexOf("Exp == "), AldorId.class, true);
        Assert.assertNotNull(expElt);
        AldorReference expRef = expElt.getReference();
        Assert.assertNotNull(expRef);
        PsiElement expResolved = expRef.resolveMacro();
        Assert.assertNotNull(expResolved);
        Assert.assertEquals("Exp ==> with", expResolved.getText());
    }

    public void testNoMacroReference_unresolved() {
        String text = "Foo(X: Integer): Exp == Impl\n"
                + " where\n"
                + "    Impl ==> add\n";
        PsiFile file = createSpadFile(text);
        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(file, text.indexOf("Exp"), AldorId.class, true);
        Assert.assertNotNull(elt);
        AldorReference ref = elt.getReference();
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolveMacro();
        Assert.assertNull(resolved);
    }

    public void testMacroReferenceToDefine_unresolved() {
        String text = "Foo(X: Integer): Exp == Impl\n"
                + " where\n"
                + "    Exp == with\n"
                + "    Impl ==> add\n";
        PsiFile file = createSpadFile(text);
        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(file, text.indexOf("Exp"), AldorId.class, true);
        Assert.assertNotNull(elt);
        AldorReference ref = elt.getReference();
        Assert.assertNotNull(ref);

        PsiElement resolved = ref.resolveMacro();
        Assert.assertNull(resolved);
    }

    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(directory);
    }

}
