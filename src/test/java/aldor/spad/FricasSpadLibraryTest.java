package aldor.spad;

import aldor.lexer.AldorTokenTypes;
import aldor.parser.SwingThreadTestRule;
import aldor.syntax.Syntax;
import aldor.syntax.components.Id;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;
import static org.junit.Assert.assertFalse;

public class FricasSpadLibraryTest {
    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux");
    private final CodeInsightTestFixture testFixture = createFixture(fricasSdkProjectDescriptor(directoryPresentRule.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testParents0() {
        FricasSpadLibrary lib = new FricasSpadLibrary(testFixture.getProject(),
                                                        ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk().getHomeDirectory());
        Syntax syntax = Id.createMissingId(AldorTokenTypes.TK_Id, "Integer");

        List<Syntax> pp = lib.parentCategories(syntax);
        for (Syntax p: pp) {
            System.out.println("Parent category: " + p);
        }
        assertFalse(pp.isEmpty());
        lib.dispose();
    }

}
