package aldor.spad;

import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.components.Apply;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;
import static com.intellij.testFramework.LightPlatformTestCase.getProject;
import static org.junit.Assert.assertNotNull;

public class SpadLibraryManagerTest {
    @Rule
    public final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux");

    private final CodeInsightTestFixture testFixture = createFixture(fricasSdkProjectDescriptor(directory.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directory)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void test() {
        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("List", getProject(), GlobalSearchScope.allScope(getProject()));

        PsiFile file = ll.iterator().next().getContainingFile();

        SpadLibrary lib = SpadLibraryManager.instance().spadLibraryForElement(file);
        assertNotNull(lib);
        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "List Integer");
        assertNotNull(syntax);
        List<Syntax> pp = lib.parentCategories(syntax);
        for (Syntax parentSyntax: pp) {
            Syntax lead = parentSyntax;
            while (lead.is(Apply.class)) {
                lead = lead.as(Apply.class).operator();
            }

            System.out.println("Parent: " + parentSyntax + " " + lead.psiElement());
        }
        List<SpadLibrary.Operation> ops = lib.operations(syntax);
        for (SpadLibrary.Operation op: ops) {
            System.out.println("Operation: " + op);
            System.out.println("name: " + op.name() + ": " + SyntaxPrinter.instance().toString(op.type()));
        }

        SpadLibrary.Operation theCons = ops.stream().filter(op -> Objects.equals("cons", op.name())).findFirst().orElse(null);
        assertNotNull(theCons);
        Assert.assertEquals("(Integer, %) -> %", SyntaxPrinter.instance().toString(theCons.type()));



    }
    @Test
    public void testRing() {
        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("Ring", getProject(), GlobalSearchScope.allScope(getProject()));

        PsiFile file = ll.iterator().next().getContainingFile();

        SpadLibrary lib = SpadLibraryManager.instance().spadLibraryForElement(file);
        assertNotNull(lib);
        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "Ring");
        assertNotNull(syntax);
        List<Syntax> pp = lib.parentCategories(syntax);
        for (Syntax parentSyntax: pp) {
            Syntax lead = parentSyntax;
            while (lead.is(Apply.class)) {
                lead = lead.as(Apply.class).operator();
            }

            System.out.println("Parent: " + parentSyntax + " " + lead.psiElement());
            assertNotNull(lead.psiElement());
        }


    }


}
