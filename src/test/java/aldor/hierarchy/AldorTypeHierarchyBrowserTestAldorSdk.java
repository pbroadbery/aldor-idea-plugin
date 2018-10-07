package aldor.hierarchy;

import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import junit.framework.AssertionFailedError;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AldorTypeHierarchyBrowserTestAldorSdk {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.AldorDev();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(aldorExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new TestRule() {
                        @Override
                        public Statement apply(Statement statement, Description description) {
                            return JUnits.prePostStatement(JUnits::setLogToInfo, () -> System.out.println("Done"), statement);
                        }
                    })
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testReference() {
        String text = "x: List X == empty()";
        PsiFile whole = codeTestFixture.addFileToProject("test.as", text);

        PsiElement elt = whole.findElementAt(text.indexOf("List"));

        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE);
        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        AldorHierarchyOperationDescriptor findAll = browser.childElements().stream()
                .filter(x -> x instanceof AldorHierarchyOperationDescriptor)
                .map(x -> (AldorHierarchyOperationDescriptor) x)
                .filter(x -> "findAll".equals(x.operation().name()))
                .findFirst().orElseThrow(AssertionFailedError::new);

        assertNotNull(findAll.operation().containingForm());
        assertNull(findAll.operation().declaration());
        PsiElement findAllElt = findAll.getPsiElement();
        assertNotNull(findAllElt);
        System.out.println("FindAll: " + findAllElt);
        assertTrue(findAllElt.getContainingFile().getVirtualFile().getPath().contains("sal_list.as"));
        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testRing() {
        String text = "Ring";
        PsiFile whole = codeTestFixture.addFileToProject("test.as", text);

        PsiElement elt = whole.findElementAt(text.indexOf("Ring"));

        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE);
        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testListType() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("ListType", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);

        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE);

        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule executableRule) {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(executableRule.prefix());
    }

}
