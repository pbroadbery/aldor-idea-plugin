package aldor.references;

import aldor.EnsureParsingTest;
import aldor.language.AldorLanguage;
import aldor.psi.AldorE6;
import aldor.psi.AldorIdentifier;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class AldorRefLookupTest extends LightPlatformCodeInsightFixtureTestCase {


    public void testReference() {
        PsiElement whole = createAldorFile("f(n: Integer): Integer == n+1");
        PsiElement theRhs = PsiTreeUtil.findChildOfType(whole, AldorE6.class);
        AldorIdentifier theRhsN = PsiTreeUtil.findChildOfType(theRhs, AldorIdentifier.class);
        assertNotNull(theRhsN);
        assertNotNull(theRhsN.getReference());
        PsiElement ref = theRhsN.getReference().resolve();
        assertNotNull(ref);
    }

    public void testLookupSingleArg() {
        String text = "f(n: Integer): Integer == n+1";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+1"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        System.out.println("Resolved: " + resolved);
        assertNotNull(resolved);
        assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupMultiArg() {
        String text = "f(n: Integer, m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupNegationDefinition() {
        String text = "-(x: X): Integer == -x";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.lastIndexOf('x'));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf('x'), resolved.getTextOffset());
    }


    public void testLookupInfixArg() {
        String text = "(+)(n: Integer, m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }



    public void testLookupNotPresent() {
        String text = "f(n: Integer): Integer == foo";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("foo"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNull(resolved);
    }


    public void testLookupInfix2Arg() {
        String text = "(n: Integer) + (m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();

        assertNotNull(resolved);
        assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupCurriedArg() {
        String text = "f(n: Integer)(m: Integer): Integer == n+m+1";
        PsiFile file = createAldorFile(text);

        PsiReference refN = file.findReferenceAt(text.indexOf("n+m"));
        assertNotNull(refN);
        PsiElement resolved = refN.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("n:"), resolved.getTextOffset());

        PsiReference refM = file.findReferenceAt(text.indexOf("m+1"));
        assertNotNull(refM);
        resolved = refM.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("m:"), resolved.getTextOffset());
    }

    public void testLookupForVar() {
        String text = "for x in 1..10 repeat foo(x)";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("x)"));
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("x in"), resolved.getTextOffset());
    }

    public void testLookupCollectionVar() {
        String text = "[x for x in 1..10]";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf('x')); // First index in this case
        assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        assertNotNull(resolved);
        assertEquals(text.indexOf("x in"), resolved.getTextOffset());
    }


    private PsiFile createAldorFile(String text) {
        return createLightFile("foo.as", AldorLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }

}
