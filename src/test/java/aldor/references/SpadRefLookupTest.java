package aldor.references;

import aldor.language.SpadLanguage;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadBinaryOp;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

public class SpadRefLookupTest extends AssumptionAware.BasePlatformTestCase {


    public void testReference() {
        PsiElement whole = createSpadFile("f(n: Integer): Integer == n+1");
        PsiElement theRhs = PsiTreeUtil.findChildOfType(whole, SpadBinaryOp.class);
        AldorIdentifier theRhsN = PsiTreeUtil.findChildOfType(theRhs, AldorIdentifier.class);
        Assert.assertNotNull(theRhsN);
        Assert.assertNotNull(theRhsN.getReference());
        PsiElement ref = theRhsN.getReference().resolve();
        Assert.assertNotNull(ref);
        Assert.assertEquals(whole.getText().indexOf("n:"), ref.getTextOffset());
    }

    public void testReferenceToDefaultRetFnArg() {
        PsiFile whole = createSpadFile("reduce(x: Fraction UP) == reduce(numer x) exquo 1");
        AldorIdentifier rhsX = PsiTreeUtil.findElementOfClassAtOffset(whole, whole.getText().indexOf("x)"), AldorIdentifier.class, true);
        Assert.assertNotNull(rhsX);
        Assert.assertNotNull(rhsX.getReference());
        PsiElement ref = rhsX.getReference().resolve();
        Assert.assertNotNull(ref);
        Assert.assertEquals(whole.getText().indexOf("x:"), ref.getTextOffset());
    }

    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }

}
