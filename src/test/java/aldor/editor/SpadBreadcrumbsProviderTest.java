package aldor.editor;

import aldor.language.SpadLanguage;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.junit.Assert;

public class SpadBreadcrumbsProviderTest extends AssumptionAware.BasePlatformTestCase {

    public void testReference() {
        String text =
                "Foo: E == I where\n" +
                "  E ==> with\n" +
                "  I ==> add\n " +
                "     a == 1";
        PsiFile whole = createSpadFile(text);
        AldorPsiUtils.logPsi(whole);
        PsiElement def = PsiTreeUtil.findElementOfClassAtOffset(whole, text.indexOf("a =="), AldorDefine.class, true);

        BreadcrumbsProvider provider = new AldorBreadcrumbsProvider();

        assert def != null;
        Assert.assertTrue(provider.acceptElement(def));
        Assert.assertEquals("a", provider.getElementInfo(def));
    }

    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }
}

