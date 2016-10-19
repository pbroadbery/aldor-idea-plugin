package aldor.syntax;

import aldor.parser.EnsureParsingTest;
import aldor.parser.ParserFunctions;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.io.PrintWriter;
import java.io.StringWriter;

import static aldor.syntax.SyntaxPsiParser.parse;

public class SyntaxPrintTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testApplyId() {
        assertEquals("f", parseAndPrint("f"));
        assertEquals("f x", parseAndPrint("f x"));
        assertEquals("f(x, y)", parseAndPrint("f(x, y)"));
        assertEquals("f f x", parseAndPrint("f f x"));
        assertEquals("(f a) x", parseAndPrint("(f a) x"));
    }

    public void testApplyMap() {
        assertEquals("A -> B", parseAndPrint("A -> B"));
    }

    private String parseAndPrint(String text) {
        PsiElement psi = parseText(text);
        Syntax syntax = parse(psi);
        assertNotNull(syntax);
        System.out.println("Syntax: " + syntax);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        SyntaxPrinter.instance().print(pw, syntax);
        return sw.toString();
    }


    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
