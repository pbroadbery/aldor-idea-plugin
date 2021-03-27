package aldor.editor;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefine;
import aldor.test_util.AssumptionAware;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.junit.Assert;

public class AldorBreadcrumbsProviderTest extends AssumptionAware.BasePlatformTestCase {

    public void testReference() {
        PsiElement whole = createAldorFile("Foo: with == add { a == 1 }");
        PsiElement def = PsiTreeUtil.findChildOfType(whole, AldorDefine.class);

        BreadcrumbsProvider provider = new AldorBreadcrumbsProvider();

        assert def != null;
        Assert.assertTrue(provider.acceptElement(def));
        Assert.assertEquals("Foo", provider.getElementInfo(def));
    }

    private PsiElement createAldorFile(String text) {
        AldorLanguage language = AldorLanguage.INSTANCE;
        return createFile(text, language);
    }

    private PsiElement createFile(String text, Language language) {
        return createLightFile("foo.as", language, text);
    }


    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }
}

