package aldor.hierarchy;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.test_util.EnsureClosedRule;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.List;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;

public class AldorTypeHierarchyBrowserTest {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));
    private final EnsureClosedRule ensureClosedRule = new EnsureClosedRule();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(fricasExecutableRule)
                    .around(ensureClosedRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testReference() {
        String text = "x: List X == empty()";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        PsiElement elt = whole.findElementAt(text.indexOf("List"));

        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE)) {

            browser.update();

            System.out.println("Root: " + browser.rootDescriptor());

            System.out.println("Children: " + browser.childElements());

            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }

    @Test
    public void testRing() {
        String text = "Ring";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        HierarchyProvider provider = new AldorTypeHierarchyProvider();
        PsiElement elt = whole.findElementAt(text.indexOf("Ring"));

        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE)) {
            browser.update();

            System.out.println("Root: " + browser.rootDescriptor());

            System.out.println("Children: " + browser.childElements());

            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }

    @Test
    public void testEltAgg() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("EltableAggregate", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);

        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE)) {

            browser.update();

            System.out.println("Root: " + browser.rootDescriptor());

            System.out.println("Children: " + browser.childElements());

            browser.dispose();
            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }



    @Test
    public void testFlatEltAgg() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("EltableAggregate", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);
        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE)) {

            browser.update();

            System.out.println("Root: " + browser.rootDescriptor());

            List<NodeDescriptor<?>> childElements = browser.childElements();
            System.out.println("Children: " + childElements);

            Assert.assertEquals(5, childElements.size());

            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }

    @Test
    public void testRightModule() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("RightModule",
                codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);
        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE)) {

            browser.update();

            /*
            AbelianSemiGroup
            *: (%, R) -> %
            Unknown element - if  then AbelianGroup else ()
            Unknown element - if  then AbelianMonoid else ()
             */

            System.out.println("Root: " + browser.rootDescriptor());

            List<NodeDescriptor<?>> childElements = browser.childElements();
            System.out.println("Children: " + childElements);

            Assert.assertEquals(4, childElements.size());

            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }


    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }
}
