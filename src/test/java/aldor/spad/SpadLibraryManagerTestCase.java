package aldor.spad;

import aldor.parser.ParserFunctions;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.components.Apply;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertNotNull;

public abstract class SpadLibraryManagerTestCase {

    public abstract String basePath();
    public abstract CodeInsightTestFixture testFixture();


    @Test
    public void xtestListInteger() {
        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("List", testFixture().getProject(), GlobalSearchScope.allScope(testFixture().getProject()));

        PsiFile file = ll.iterator().next().getContainingFile();

        SpadLibrary lib = SpadLibraryManager.instance().spadLibraryForElement(file);
        assertNotNull(lib);
        Syntax syntax = ParserFunctions.parseToSyntax(testFixture().getProject(), "List Integer");
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
    public void xtestRing() {
        Collection<AldorDefine> ll = AldorDefineTopLevelIndex.instance.get("Ring", testFixture().getProject(), GlobalSearchScope.allScope(testFixture().getProject()));

        PsiFile file = ll.iterator().next().getContainingFile();

        SpadLibrary lib = SpadLibraryManager.instance().spadLibraryForElement(file);
        assertNotNull(lib);
        Syntax syntax = ParserFunctions.parseToSyntax(testFixture().getProject(), "Ring");
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
