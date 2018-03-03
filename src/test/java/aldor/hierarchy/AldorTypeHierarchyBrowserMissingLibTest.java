package aldor.hierarchy;

import aldor.lexer.AldorTokenTypes;
import aldor.parser.SwingThreadTestRule;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.components.Id;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.Assertions;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.List;

public class AldorTypeHierarchyBrowserMissingLibTest {
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor(fricasExecutableRule));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(fricasExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(new SwingThreadTestRule());

    //@Test
    @Ignore("Test causes trouble due to the sdk setup step")
    public void xtestReference() {
        Sdk projectSdk = Assertions.isNotNull(ProjectRootManager.getInstance(codeTestFixture.getProject()).getProjectSdk());

        SpadLibraryManager.instance().spadLibraryForSdk(projectSdk, new MockSpadLibrary());

        String text = "x: List X == empty()";
        PsiFile whole = codeTestFixture.addFileToProject("test.spad", text);

        HierarchyProvider provider = new AldorTypeHierarchyProvider();
        PsiElement elt = whole.findElementAt(text.indexOf("List"));

        DataContext context = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), elt,
                SimpleDataContext.getProjectContext(codeTestFixture.getProject()));

        PsiElement target = provider.getTarget(context);
        AldorTypeHierarchyBrowser browser = (AldorTypeHierarchyBrowser) provider.createHierarchyBrowser(target);

        /*
         * This test needs a bit of fixing to ensure that the MockSpadLibrary is used,and
         * then confirm that the result looks ok.
         */

        browser.dispose();
        ((Disposable) ProgressManager.getInstance()).dispose();
    }

    private static LightProjectDescriptor getProjectDescriptor(ExecutablePresentRule fricasExecutableRule) {
        return SdkProjectDescriptors.fricasSdkProjectDescriptor(fricasExecutableRule.prefix());

    }

    private class MockSpadLibrary implements SpadLibrary {
        @Override
        public List<Syntax> parentCategories(Syntax syntax) {
            return Collections.singletonList(Id.createMissingId(AldorTokenTypes.TK_Id, "Something"));
        }

        @Override
        public List<Operation> operations(Syntax syntax) {
            return Collections.emptyList();
        }

        @NotNull
        @Override
        public Syntax normalise(@NotNull Syntax syntax) {
            return syntax;
        }

        @Override
        public List<Syntax> allTypes() {
            return Collections.emptyList();
        }

        @Override
        public String definingFile(Id id) {
            return "nope";
        }
    }
}
