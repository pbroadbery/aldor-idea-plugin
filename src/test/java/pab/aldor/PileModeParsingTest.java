package pab.aldor;

import aldor.AldorTypes;
import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

import java.util.List;

import static pab.aldor.ParserFunctions.getPsiErrorElements;
import static pab.aldor.ParserFunctions.logPsi;

public class PileModeParsingTest extends LightPlatformCodeInsightTestCase{


    public void testPileMode() {
        PsiElement psi = parseText("#pile\nrepeat\n  foo\n  bar");
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testPileAfter2Eq() {
        PsiElement psi = parseText("#pile\nfoo: X == \n 1\n 2");
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testTwoStatements() {
        PsiElement psi = parseText("#pile\nA\nB\n");
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testComplexThenSimple() {
        PsiElement psi = parseText("#pile\nrepeat\n Foo\nA := 1");
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);

        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());
        unit.start("#pile\nrepeat\n Foo\nA := 1");
        assertEquals(0, errors.size());
    }

    public void testComplexThenComplex() {
        PsiElement psi = parseText("#pile\nrepeat\n Foo\nrepeat\n a");
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    // NB: Comma is probably wrong...
    private PsiElement parseText(CharSequence text) {
        return parseText(text, AldorTypes.TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseText(getProject(), text, elementType);
    }

}
