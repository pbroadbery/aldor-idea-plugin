package aldor.references;

import aldor.language.AldorLanguage;
import aldor.psi.AldorIdentifier;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class VariantScopeTest extends LightPlatformCodeInsightFixtureTestCase {

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
