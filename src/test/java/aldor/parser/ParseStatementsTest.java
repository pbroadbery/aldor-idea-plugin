package aldor.parser;

import aldor.psi.AldorId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static aldor.psi.AldorPsiUtils.logPsi;

/**
 * Statements have lots of special cases.. Let's try to catch some of them.
 */
public class ParseStatementsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testNormalStatementSequence() {
        PsiElement psi = parseText("a;b;c");
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
        logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "a".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "b".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "c".equals(elt.getText())).size() == 1);
    }

    public void testNormalSubStatementSequence() {
        PsiElement psi = parseText("return {a;b;c}");
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
        logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "a".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "b".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "c".equals(elt.getText())).size() == 1);
    }

    public void testReturnSubStatementSequence() {
        PsiElement psi = parseText("x + {a;b;c}");
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
        logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "a".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "b".equals(elt.getText())).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "c".equals(elt.getText())).size() == 1);
    }


    public void testImplicitSemicolon() {
        PsiElement psi = parseText("x == {x} y == 2");
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
        logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "x".equals(elt.getText())).size() == 2);
        assertTrue(ParserFunctions.find(psi, elt -> (elt instanceof AldorId) && "y".equals(elt.getText())).size() == 1);
    }

    public void testEmptyStatementFails() {
        PsiElement psi = parseText("a;;b");
        logPsi(psi);
        assertFalse(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testEmptyBlock() {
        PsiElement psi = parseText("{repeat {};}\n");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testEmptyPenultimateBlock() {
        PsiElement psi = parseText("{repeat {};1}\n");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testNoSemicolonAtOneStmtBlockEnd() {
        PsiElement psi = parseText("return {foo}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX1() {
        PsiElement psi = parseText("while X repeat {} p;");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX2() {
        PsiElement psi = parseText("repeat {}repeat {} p;");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX4() {
        PsiElement psi = parseText("{repeat {} while X repeat {}}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testSequenceOfWhile() {
        PsiElement psi = parseText("repeat {}while X repeat {}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSemicolonAtOneStmtBlockEndFails() {
        PsiElement psi = parseText("{foo;}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testNoSemicolonAtBlockEnd() {
        PsiElement psi = parseText("return {x;foo}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSemicolonAtBlockEnd() {
        PsiElement psi = parseText("{y;foo;}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testSemicolonAfterBlockEnd() {
        PsiElement psi = parseText("repeat {foo};y = 3");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testIfStatement() {
        PsiElement psi = parseText("if X then Y");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testIfElseStatement() {
        PsiElement psi = parseText("if X then Y else Z");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testIfSemicolonElseStatement() {
        PsiElement psi = parseText("if X then Y; else Z");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testIfElseElseIfStatement() {
        PsiElement psi = parseText("if x then Y else if Z then W");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testIfElseElseIfElseStatement() {
        PsiElement psi = parseText("if X then Y else if Z then W else V");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testExportStatement() {
        PsiElement psi = parseText("export { QQQ: AAA } ");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testExportToStatement() {
        PsiElement psi = parseText("export { A: X } to Y");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testExportFromStatement() {
        PsiElement psi = parseText("export { A: X } from Y");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testDefinitions() {
        PsiElement psi = parseText("Foo(R: Ring): with { a: Y; b: Z } == add {}");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }

    public void testSimpleDefinition() {
        PsiElement psi = parseText("f(n: Integer): Integer == n+1");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    public void testSimpleDefinition2() {
        PsiElement psi = parseText("A == B == C");
        logPsi(psi);
        assertTrue(ParserFunctions.getPsiErrorElements(psi).isEmpty());
    }


    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }


    private PsiElement parseText(CharSequence text, IElementType type) {
        return ParserFunctions.parseAldorText(getProject(), text, type);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }



}
