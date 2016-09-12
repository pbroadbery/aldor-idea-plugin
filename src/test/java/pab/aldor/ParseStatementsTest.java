package pab.aldor;

import aldor.psi.AldorId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static pab.aldor.ParserFunctions.getPsiErrorElements;
import static pab.aldor.ParserFunctions.logPsi;

/**
 * Statements have lots of special cases.. Let's try to catch some of them.
 */
public class ParseStatementsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testNormalStatementSequence() {
        PsiElement psi = parseText("a;b;c");
        assertTrue(getPsiErrorElements(psi).isEmpty());
        ParserFunctions.logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("a")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("b")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("c")).size() == 1);
    }

    public void testNormalSubStatementSequence() {
        PsiElement psi = parseText("return {a;b;c}");
        assertTrue(getPsiErrorElements(psi).isEmpty());
        ParserFunctions.logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("a")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("b")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("c")).size() == 1);
    }

    public void testReturnSubStatementSequence() {
        PsiElement psi = parseText("x + {a;b;c}");
        assertTrue(getPsiErrorElements(psi).isEmpty());
        ParserFunctions.logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("a")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("b")).size() == 1);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("c")).size() == 1);
    }


    public void testImplicitSemicolon() {
        PsiElement psi = parseText("x == {x} y == 2");
        assertTrue(getPsiErrorElements(psi).isEmpty());
        ParserFunctions.logPsi(psi);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("x")).size() == 2);
        assertTrue(ParserFunctions.find(psi, elt -> elt instanceof AldorId && elt.getText().equals("y")).size() == 1);
    }

    public void testEmptyStatementFails() {
        PsiElement psi = parseText("a;;b");
        logPsi(psi);
        assertFalse(getPsiErrorElements(psi).isEmpty());
    }

    public void testEmptyBlock() {
        PsiElement psi = parseText("{repeat {};}\n");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testEmptyPenultimateBlock() {
        PsiElement psi = parseText("{repeat {};1}\n");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testNoSemicolonAtOneStmtBlockEnd() {
        PsiElement psi = parseText("return {foo}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX1() {
        PsiElement psi = parseText("while X repeat {} p;");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX2() {
        PsiElement psi = parseText("repeat {}repeat {} p;");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testSequenceOfWhileX4() {
        PsiElement psi = parseText("{repeat {} while X repeat {}}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testSequenceOfWhile() {
        PsiElement psi = parseText("repeat {}while X repeat {}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testSemicolonAtOneStmtBlockEndFails() {
        PsiElement psi = parseText("{foo;}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testNoSemicolonAtBlockEnd() {
        PsiElement psi = parseText("return {x;foo}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testSemicolonAtBlockEnd() {
        PsiElement psi = parseText("{y;foo;}");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testSemicolonAfterBlockEnd() {
        PsiElement psi = parseText("repeat {foo};y = 3");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testIfStatement() {
        PsiElement psi = parseText("if X then Y");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    public void testIfElseStatement() {
        PsiElement psi = parseText("if X then Y else Z");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testIfSemicolonElseStatement() {
        PsiElement psi = parseText("if X then Y; else Z");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testIfElseElseIfStatement() {
        PsiElement psi = parseText("if x then Y else if Z then W");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testIfElseElseIfElseStatement() {
        PsiElement psi = parseText("if X then Y else if Z then W else V");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testExportStatement() {
        PsiElement psi = parseText("export { QQQ: AAA } ");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testExportToStatement() {
        PsiElement psi = parseText("export { A: X } to Y");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }


    public void testExportFromStatement() {
        PsiElement psi = parseText("export { A: X } from Y");
        logPsi(psi);
        assertTrue(getPsiErrorElements(psi).isEmpty());
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseText(getProject(), text);
    }


    private PsiElement parseText(CharSequence text, IElementType type) {
        return ParserFunctions.parseText(getProject(), text, type);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }



}