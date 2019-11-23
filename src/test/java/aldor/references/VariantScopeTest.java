package aldor.references;

import aldor.language.AldorLanguage;
import aldor.language.SpadLanguage;
import aldor.psi.AldorIdentifier;
import aldor.test_util.JUnits;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static aldor.psi.AldorPsiUtils.logPsi;

public class VariantScopeTest extends BasePlatformTestCase {

    public void testFunctionReference() {
        PsiFile whole = createAldorFile("f(n: Integer, m: Integer): Integer == n+1");

        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(whole, whole.getText().indexOf("n+1"), AldorIdentifier.class, true);
        Assert.assertTrue((elt != null) && (elt.getReference() != null));
        Set<String> items = Arrays.stream(elt.getReference().getVariants()).map(this::name).collect(Collectors.toSet());
        Assert.assertEquals(Sets.newHashSet("f", "m", "n"), items);
    }

    public void testAddBodyReference() {
        PsiFile whole = createAldorFile("Foo(X: A): with == add { f(n: Integer, m: Integer): Integer == n+1}");

        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(whole, whole.getText().indexOf("n+1"), AldorIdentifier.class, true);
        Assert.assertTrue((elt != null) && (elt.getReference() != null));
        Set<String> items = Arrays.stream(elt.getReference().getVariants()).map(this::name).collect(Collectors.toSet());
        Assert.assertEquals(Sets.newHashSet("Foo", "X", "f", "m", "n"), items);
    }

    public void testAddBodyMultiReference() {
        PsiFile whole = createAldorFile("Foo(X: A): with == add { f(n: Integer): () == qq; g(x: X): () == never }");

        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(whole, whole.getText().indexOf("qq"), AldorIdentifier.class, true);
        Assert.assertTrue((elt != null) && (elt.getReference() != null));
        List<String> items = Arrays.stream(elt.getReference().getVariants()).map(this::name).collect(Collectors.toList());
        Assert.assertEquals(items.size(), new HashSet<>(items).size());
        Assert.assertEquals(Sets.newHashSet("Foo", "X", "f", "n", "g"), new HashSet<>(items));
    }

    public void testAddBodySpadMultiReference() {
        JUnits.setLogToInfo();
        PsiFile whole = createSpadFile("Foo(X: A): with == add\n" +
                "    f(n: Integer): () == qq\n" +
                "    g(x: X): () == never\n");
        logPsi(whole);
        AldorIdentifier elt = PsiTreeUtil.findElementOfClassAtOffset(whole, whole.getText().indexOf("qq"), AldorIdentifier.class, true);
        Assert.assertTrue((elt != null) && (elt.getReference() != null));
        Object[] variants = elt.getReference().getVariants();
        System.out.println("Elts: " + Arrays.stream(variants).map(this::text).collect(Collectors.toList()));
        List<String> items = Arrays.stream(variants).map(this::name).collect(Collectors.toList());
        Assert.assertEquals(Sets.newHashSet("Foo", "X", "f", "n", "g"), new HashSet<>(items));
        System.out.println("items: " + items);
        Assert.assertEquals(items.size(), new HashSet<>(items).size());
    }

    private String name(Object x) {
        //noinspection ChainOfInstanceofChecks
        if (x instanceof LookupElement) {
            return ((LookupElement) x).getLookupString();
        }
        if (x instanceof PsiElement) {
            return ((PsiElement) x).getText();
        }
        return "";
    }

    private String text(Object x) {
        //noinspection ChainOfInstanceofChecks
        if (x instanceof LookupElement) {
            return Objects.requireNonNull(((LookupElement) x).getPsiElement()).getText();
        }
        else if (x instanceof PsiElement) {
            return ((PsiElement) x).getText();
        }
        return x.toString();
    }

    private PsiFile createAldorFile(String text) {
        return createFile(text, AldorLanguage.INSTANCE);
    }

    private PsiFile createSpadFile(String text) {
        return createFile(text, SpadLanguage.INSTANCE);
    }


    private PsiFile createFile(String text, Language language) {
        return createLightFile("foo.as", language, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }

}
