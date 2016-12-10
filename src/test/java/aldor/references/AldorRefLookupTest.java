package aldor.references;

import aldor.language.AldorLanguage;
import aldor.parser.EnsureParsingTest;
import aldor.psi.AldorE6;
import aldor.psi.AldorIdentifier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static aldor.psi.AldorPsiUtils.logPsi;

public class AldorRefLookupTest extends LightPlatformCodeInsightFixtureTestCase {


    public void testReference() {
        PsiElement whole = createAldorFile("f(n: Integer): Integer == n+1");
        PsiElement theRhs = PsiTreeUtil.findChildOfType(whole, AldorE6.class);
        AldorIdentifier theRhsN = PsiTreeUtil.findChildOfType(theRhs, AldorIdentifier.class);
        Assert.assertNotNull(theRhsN);
        Assert.assertNotNull(theRhsN.getReference());
        PsiElement ref = theRhsN.getReference().resolve();
        Assert.assertNotNull(ref);
    }

    public void testLookupSingleArg() {
        String text = "f(n: Integer): Integer == n+1";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("n+1"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupLocal() {
        String text = "local f(n: Integer): Integer == n+1";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+1"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupDefine() {
        String text = "define f(n: Integer): Integer == n+1";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+1"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupMultiArg() {
        String text = "f(n: Integer, m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupNegationDefinition() {
        String text = "-(x: X): Integer == -x";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.lastIndexOf('x'));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf('x'), resolved.getTextOffset());
    }


    public void testLookupInfixArg() {
        String text = "(+)(n: Integer, m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }



    public void testLookupNotPresent() {
        String text = "f(n: Integer): Integer == foo";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("foo"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNull(resolved);
    }


    public void testLookupInfix2Arg() {
        String text = "(n: Integer) + (m: Integer): Integer == n+m";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("n+m"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();

        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());
    }

    public void testLookupCurriedArg() {
        String text = "f(n: Integer)(m: Integer): Integer == n+m+1";
        PsiFile file = createAldorFile(text);

        PsiReference refN = file.findReferenceAt(text.indexOf("n+m"));
        Assert.assertNotNull(refN);
        PsiElement resolved = refN.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("n:"), resolved.getTextOffset());

        PsiReference refM = file.findReferenceAt(text.indexOf("m+1"));
        Assert.assertNotNull(refM);
        resolved = refM.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("m:"), resolved.getTextOffset());
    }

    public void testLookupForVar() {
        String text = "for x in 1..10 repeat foo(x)";
        PsiFile file = createAldorFile(text);
        PsiReference ref = file.findReferenceAt(text.indexOf("x)"));
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("x in"), resolved.getTextOffset());
    }

    public void testLookupCollectionVar() {
        String text = "[x for x in 1..10]";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf('x')); // First index in this case
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("x in"), resolved.getTextOffset());
    }

    public void disabled_testLookupAssignment() {
        String text = "foo(): () == { x := 1; x}";
        PsiFile file = createAldorFile(text);

        PsiReference ref = file.findReferenceAt(text.indexOf("x}"));
        logPsi(file);
        Assert.assertNotNull(ref);
        PsiElement resolved = ref.resolve();
        Assert.assertNotNull(resolved);
        Assert.assertEquals(text.indexOf("x :="), resolved.getTextOffset());
    }

    public void testOneReference() {
        String text = "foo(x: I): I == x + 1";
        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("x + 1", "x: I")
                .build();
        Set<String> nulls = Collections.singleton("x: I");
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSequenceReferencesFn() {
        String text = "#pile\n"+
                "f(h: A): % == h + 1\n" +
                "g(h: B): % == h + 2";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("h + 1", "h: A")
                .put("h + 2", "h: B")
                .build();
        Collection<String> nulls = Arrays.asList("h: A", "h: B");
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSequenceOp() {
        String text = "#pile\n"+
                "f(h: A): % == h + 1\n" +
                "-(h: B): % == h + 2\n";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("h + 1", "h: A")
                .put("h + 2", "h: B")
                .build();
        Collection<String> nulls = Arrays.asList("h: A", "h: B");
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSequenceOp2() {
        String text = "#pile\n\n"+
                "f(h: A): % == h\n\n" +
                "-(h: B): % == h + 2\n";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("h\n", "h: A")
                .put("h + 2", "h: B")
                .build();
        Collection<String> nulls = Arrays.asList("h: A", "h: B");
        assertReferences(text, nameRefToMap, nulls);
    }


    public void testLambdaRef() {
        String text = "#pile\n"+
                "(a: %): S +-> a+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("a+1", "a: %")
                .build();
        Collection<String> nulls = Collections.singletonList("a: %");
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testLambda2Ref() {
        String text = "#pile\n"+
                "(a: %, b: %): S +-> a+b+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("a+b", "a: %")
                .put("b+1", "b: %")
                .build();
        Collection<String> nulls = Arrays.asList("a: %", "b: %");
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testCommaRef() {
        String text = "#pile\n"+
                "foo(n: I, R: M n): M n == n+R+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("n)", "n: I")
                .put("n+R", "n: I")
                .put("R+1", "R: M n")
                .build();
        Collection<String> nulls = Arrays.asList("n: I", "R: M n");
        assertReferences(text, nameRefToMap, nulls);
    }


    private void assertReferences(String text, Map<String, String> nameRefToMap, Iterable<String> nulls) {
        PsiFile file = createAldorFile(text);
        logPsi(file);
        Map<String, PsiElement> refMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry: nameRefToMap.entrySet()) {
            PsiReference ref = file.findReferenceAt(text.indexOf(entry.getKey()));
            Assert.assertNotNull("Failed: " + entry.getValue(), ref);
            PsiElement resolved = ref.resolve();
            Assert.assertNotNull("Failed: " + entry, resolved);

            refMap.put(entry.getKey(), resolved);
        }

        for (Map.Entry<String, String> entry: nameRefToMap.entrySet()) {
            Assert.assertEquals("Failed to ref: " + entry, text.indexOf(entry.getValue()), refMap.get(entry.getKey()).getTextOffset());
        }

        for (String txt: nulls) {
            PsiReference ref = file.findReferenceAt(text.indexOf(txt));
            Assert.assertNotNull(ref);
            PsiElement resolved = ref.resolve();
            Assert.assertNull("Expecting no ref for "+ txt, resolved);
        }
    }

    private PsiFile createAldorFile(String text) {
        return createLightFile("foo.as", AldorLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }

}
