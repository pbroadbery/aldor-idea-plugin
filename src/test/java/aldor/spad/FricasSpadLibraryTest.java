package aldor.spad;

import aldor.lexer.AldorTokenTypes;
import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;
import static com.intellij.testFramework.LightPlatformTestCase.getProject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
        VirtualFile homeDirectory = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk().getHomeDirectory();
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder().project(testFixture.getProject())
                .daaseDirectory(homeDirectory.findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();
        Syntax syntax = Id.createMissingId(AldorTokenTypes.TK_Id, "Integer");

        List<Syntax> pp = lib.parentCategories(syntax);
        for (Syntax p: pp) {
            System.out.println("Parent category: " + p);
        }
        assertFalse(pp.isEmpty());
        lib.dispose();
    }

    @Test
    public void testOperations() {
        JUnits.setLogToInfo();
        VirtualFile homeDirectory = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk().getHomeDirectory();
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder().project(testFixture.getProject())
                .daaseDirectory(homeDirectory.findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();

        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("Group", getProject(), GlobalSearchScope.allScope(getProject()));

        Syntax syntax = SyntaxPsiParser.parse(ll.iterator().next().lhs()).as(DeclareNode.class).lhs();
        System.out.println("Syntax is " + syntax);
        List<SpadLibrary.Operation> pp = lib.operations(syntax);
        for (SpadLibrary.Operation p: pp) {
            System.out.println("Operation: " + p + " "+ p.declaration());
        }

        pp.forEach(op -> assertNotNull(op.declaration()));

        assertFalse(pp.isEmpty());
        List<PsiElement> decls = pp.stream().map(SpadLibrary.Operation::declaration).collect(Collectors.toList());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("commutator")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("conjugate")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("inv")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("^")).count());
        assertEquals(1, decls.stream().filter(decl -> decl.getText().contains("/")).count());
        lib.dispose();
    }

    @Test
    public void testCoercibleToOperations() {
        JUnits.setLogToInfo();
        VirtualFile homeDirectory = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk().getHomeDirectory();
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(homeDirectory.findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "CoercibleTo OutputForm");

        System.out.println("Syntax is " + syntax);
        List<SpadLibrary.Operation> pp = lib.operations(syntax);
        for (SpadLibrary.Operation p: pp) {
            System.out.println("Operation: " + p + " "+ p.declaration());
        }
        assertFalse(pp.isEmpty());
        pp.forEach(op -> assertNotNull(op.declaration()));
        lib.dispose();
    }


    @Test
    public void testListAggregrateRec() {
        JUnits.setLogToInfo();
        VirtualFile homeDirectory = ProjectRootManager.getInstance(testFixture.getProject()).getProjectSdk().getHomeDirectory();
        FricasSpadLibrary lib = new FricasSpadLibraryBuilder()
                .project(testFixture.getProject())
                .daaseDirectory(homeDirectory.findFileByRelativePath("algebra"))
                .createFricasSpadLibrary();

        Syntax syntax = ParserFunctions.parseToSyntax(testFixture.getProject(), "ListAggregate X");

        List<Syntax> parents = lib.parentCategories(syntax);
        for (Syntax parent: parents) {
            List<SpadLibrary.Operation> pp = lib.operations(parent);
            for (SpadLibrary.Operation p: pp) {
                System.out.println("Operation: " + p + " "+ p.declaration());
            }
        }

        lib.dispose();
    }

}
