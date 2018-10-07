package aldor.hierarchy;

import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
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
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;
import java.util.List;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;

public class AldorTypeHierarchyBrowserTest {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new TestRule() {
                        @Override
                        public Statement apply(Statement statement, Description description) {
                            return JUnits.prePostStatement(JUnits::setLogToInfo, () -> System.out.println("Done"), statement);
                        }
                    })
                    .around(fricasExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testReference() {
        String text = "x: List X == empty()";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        PsiElement elt = whole.findElementAt(text.indexOf("List"));

        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE);

        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testRing() {
        String text = "Ring";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        HierarchyProvider provider = new AldorTypeHierarchyProvider();
        PsiElement elt = whole.findElementAt(text.indexOf("Ring"));


        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE);
        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testEltAgg() {
        String text = "Ring";

        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("EltableAggregate", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);

        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE);

        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }



    @Test
    public void testFlatEltAgg() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("EltableAggregate", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);
        TestBrowser browser = new TestBrowser(new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE);

        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        List<NodeDescriptor<?>> childElements = browser.childElements();
        System.out.println("Children: " + childElements);

        Assert.assertEquals(5, childElements.size());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }
}
