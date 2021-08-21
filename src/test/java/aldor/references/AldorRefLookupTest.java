package aldor.references;

import aldor.language.AldorLanguage;
import aldor.language.SpadLanguage;
import aldor.psi.AldorE6;
import aldor.psi.AldorIdentifier;
import aldor.test_util.AssumptionAware;
import aldor.test_util.JUnits;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static aldor.psi.AldorPsiUtils.logPsi;

public class AldorRefLookupTest extends AssumptionAware.BasePlatformTestCase {

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


    public void testLookupInfix() {
        String text = "(n: Integer) + (m: Integer): Integer == n+m";
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
        String text = "(n: Integer) + (m: Integer): Integer == n+m+1";
        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("n+", "n:")
                .put("m+", "m:")
                .put("n", "n: Integer")
                .put("m", "m: Integer")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testLookupInfix2ArgSpad() {
        String text = "n+m == n+1+m+1";
        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("n+1", "n+m")
                .put("m+1", "m ")
                .put("n+m", "n+m")
                .put("m ", "m ")
                .build();
        Collection<String> nulls = Collections.emptyList();
        assertReferences(text, nameRefToMap, nulls, SpadLanguage.INSTANCE);
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
                .put("x: I", "x: I")
                .build();
        Set<String> nulls = Collections.emptySet();
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
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSequenceOp() {
        String text = "#pile\n"+
                "f(h: A): % == h + 1\n" +
                "-(h: B): % == h + 2\n";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("h + 1", "h: A")
                .put("h + 2", "h: B")
                .put("h: A", "h: A")
                .put("h: B", "h: B")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSequenceOp2() {
        String text = "#pile\n\n"+
                "f(h: A): % == h\n\n" +
                "-(h: B): % == h + 2\n";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("h\n", "h: A")
                .put("h + 2", "h: B")
                .put("h: A", "h: A")
                .put("h: B", "h: B")
                .build();
        assertReferences(text, nameRefToMap, Collections.emptySet());
    }


    public void testLambdaRef() {
        String text = "#pile\n"+
                "(a: %): S +-> a+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("a+1", "a: %")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testLambda2Ref() {
        String text = "#pile\n"+
                "(a: %, b: %): S +-> a+b+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("a+b", "a: %")
                .put("b+1", "b: %")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testCommaRef() {
        String text = "#pile\n"+
                "foo(n: I, R: M n): M n == n+R+1";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("n)", "n: I")
                .put("n+R", "n: I")
                .put("R+1", "R: M n")
                .put("n: I", "n: I")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testSeqRef() {
        String text = "#pile\n"
                + "f(a: A): X == a+1\n"
                + "g(a: B): X == a+2";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("a+1", "a: A")
                .put("a+2", "a: B")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testDefaultedParamRef() {
        String text = "#pile\n"
                + "foo(x): I == x+1\n";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("x+1", "x)")
                .put("x", "x")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testWhere2() {
        String text = "foo:: bar where { foo == 2; bar == 3}";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("foo:: bar where", "foo ==")
                .put("bar where", "bar ==")
                .put("foo =", "foo =")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testWhere1() {
        String text = "foo where foo == 2";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("foo where", "foo =")
                .put("foo =", "foo =")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testWhereMacro2() {
        String text = "Foo: E == I where { E ==> with; I ==> add }";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                .put("E == ", "E ==> ")
                .put("I where ", "I ==> ")
                .put("E ==>", "E ==>")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testWhereMacro3() {
        String text = "Foo(X: A): E == I where { E ==> with; I ==> add foo(a: X) == 1}";

        Map<String, String> nameRefToMap = ImmutableMap.<String, String>builder()
                //.put("X)", "X: A") -- sort of dubious reference, as LHS of where could define multiple 'X'
                // route would be
                // - find refs for X.. see that usage is in a macro
                // - find usages of macro (likely 1), for each usage, find definition for X
                .put("E == ", "E ==> ")
                .put("I where ", "I ==> ")
                .put("E ==>", "E ==>")
                .build();
        Collection<String> nulls = Collections.emptySet();
        assertReferences(text, nameRefToMap, nulls);
    }

    public void testTopLevelScope() {
        JUnits.setLogToDebug();
        String text = "X == 2; Foo(X)";

        assertReferences(text, ImmutableMap.<String, String>builder()
                .put("X)", "X ==").build(), Collections.emptySet());
    }

    public void testMacroTopLevel() {
        JUnits.setLogToDebug();
        String text = "macro Z == Integer; Foo(Z)";

        assertReferences(text, ImmutableMap.<String, String>builder()
                .put("Z)", "Z ==").build(), Collections.emptySet());
    }

    private void assertReferences(String text, Map<String, String> nameRefToMap, Iterable<String> nulls) {
        assertReferences(text, nameRefToMap, nulls, AldorLanguage.INSTANCE);
    }

    private void assertReferences(String text, Map<String, String> nameRefToMap, Iterable<String> nulls, Language language) {
        PsiFile file = createFile(text, language);
        Map<String, PsiElement> refMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry: nameRefToMap.entrySet()) {
            PsiReference ref = file.findReferenceAt(text.indexOf(entry.getKey()));
            Assert.assertNotNull("Failed to find reference: " + entry.getValue(), ref);
            PsiElement resolved = ref.resolve();
            Assert.assertNotNull("Failed to resolve: " + entry, resolved);

            refMap.put(entry.getKey(), resolved);
        }

        for (Map.Entry<String, String> entry: nameRefToMap.entrySet()) {
            Assert.assertEquals("Failed to ref: " + entry + " found " + refMap.get(entry.getKey()), text.indexOf(entry.getValue()), refMap.get(entry.getKey()).getTextOffset());
        }

        for (String txt: nulls) {
            PsiReference ref = file.findReferenceAt(text.indexOf(txt));
            Assert.assertNotNull(ref);
            PsiElement resolved = ref.resolve();
            Assert.assertNull("Expecting no ref for "+ txt, resolved);
        }
    }

    private PsiFile createAldorFile(String text) {
        AldorLanguage language = AldorLanguage.INSTANCE;
        return createFile(text, language);
    }

    private PsiFile createFile(String text, Language language) {
        return createLightFile("foo.as", language, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }

}
