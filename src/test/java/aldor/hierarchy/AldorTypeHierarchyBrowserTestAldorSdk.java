package aldor.hierarchy;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.test_util.EnsureClosedRule;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.Streams;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AldorTypeHierarchyBrowserTestAldorSdk {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.AldorDev();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(aldorExecutableRule));
    private final EnsureClosedRule ensureClosedRule = new EnsureClosedRule();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(ensureClosedRule)
                    .around(JUnits.prePostTestRule(() -> codeTestFixture.getProject().save(), () -> {}))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testReference() {
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, codeTestFixture.getProject(), null);

        String text = "x: List X == empty()";
        PsiFile whole = codeTestFixture.addFileToProject("xtest.as", text);

        PsiElement elt = whole.findElementAt(text.indexOf("List"));

        try (TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE)) {
            browser.update();

            System.out.println("Root: " + browser.rootDescriptor());

            System.out.println("Children: " + browser.childElements());
            System.out.println("Children: " + browser.childElements().stream().map(Object::getClass).collect(Collectors.toList()));

            AldorHierarchyOperationDescriptor findAll = browser.childElements().stream()
                    .flatMap(Streams.filterAndCast(AldorHierarchyOperationDescriptor.class))
                    .peek(x -> System.out.println("Found Child " + x))
                    .filter(x -> "first".equals(x.operation().name()))
                    .findFirst()
                    //.orElseThrow(AssertionFailedError::new);
                    .orElse(null);

            assertNotNull(findAll.operation().containingForm());
            assertNull(findAll.operation().declaration());
            PsiElement findAllElt = findAll.getPsiElement();
            assertNotNull(findAllElt);
            System.out.println("FindAll: " + findAllElt);
            assertTrue(findAllElt.getContainingFile().getVirtualFile().getPath().contains("sal_list.as"));
        } finally {
            ((Disposable) ProgressManager.getInstance()).dispose();
        }
    }

    @Test
    public void testGrouped() {
        String text = "Ring";
        PsiFile whole = codeTestFixture.addFileToProject("xtest.as", text);

        PsiElement elt = whole.findElementAt(text.indexOf("Ring"));

        TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), elt, AldorTypeHierarchyConstants.GROUPED_HIERARCHY_TYPE);
        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        //noinspection Convert2MethodRef
        System.out.println("Children: " + browser.childElements().stream().map(e -> e.getClass()).collect(Collectors.toSet()));

        Optional<NodeDescriptor<?>> groupedElement = browser.childElements().stream().filter(e -> e instanceof GroupingHierarchyDescriptor).findFirst();
        DocumentationManager.getInstance(codeTestFixture.getProject());
        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testRing() {
        String text = "Ring";
        PsiFile whole = codeTestFixture.addFileToProject("xtest.as", text);

        PsiElement elt = whole.findElementAt(text.indexOf("Ring"));

        TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), elt, SUPERTYPES_HIERARCHY_TYPE);
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

        TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE);

        browser.update();

        System.out.println("Root: " + browser.rootDescriptor());

        System.out.println("Children: " + browser.childElements());

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    @Test
    public void testSymbol() {
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get("Symbol", codeTestFixture.getProject(), GlobalSearchScope.allScope(codeTestFixture.getProject()));

        AldorIdentifier theId = items.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null);

        TestBrowser browser = new TestBrowser(ensureClosedRule, new AldorTypeHierarchyProvider(), theId, SUPERTYPES_HIERARCHY_TYPE);

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
