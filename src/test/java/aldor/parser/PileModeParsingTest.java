package aldor.parser;

import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import aldor.lexer.LexerFunctions;
import aldor.psi.elements.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.junit.Assert;

import java.util.List;

import static aldor.psi.AldorPsiUtils.logPsi;

public class PileModeParsingTest extends LightPlatformCodeInsightTestCase {


    public void testPileMode() {
        PsiElement psi = parseText("#pile\nrepeat\n  foo\n  bar");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }


    public void testPileAfter2Eq() {
        PsiElement psi = parseText("#pile\nfoo: X == \n 1\n 2");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testTwoStatements() {
        PsiElement psi = parseText("#pile\nA\nB\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testComplexThenSimple() {
        PsiElement psi = parseText("#pile\nrepeat\n Foo\nA := 1");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);

        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());
        unit.start("#pile\nrepeat\n Foo\nA := 1");
        Assert.assertEquals(0, errors.size());
    }

    public void testComplexThenComplex() {
        PsiElement psi = parseText("#pile\nrepeat\n Foo\nrepeat\n a");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testContinuationLine() {
        PsiElement psi = parseText("#pile\nfoo := [a,\nb]\nb\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testMultipleStatements() {
        PsiElement psi = parseText("#pile\nf == \n repeat\n  L1\n return X\n\nQ:=2\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testIfExpression() {
        PsiElement psi = parseText("" +
                "#pile\n" +
                "if X then\n" +
                "    done := true\n" +
                "else\n" +
                "    next := read()");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testIfElseExpression() {
        PsiElement psi = parseText("" +
                "#pile\n" +
                "if X then A\n" +
                "else if Y then B\n" +
                "else C");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testPiledDeclaration() {
        // Currently broken, needs fixing
        PsiElement psi = parseText("" +
                "#pile\n" +
                "Foo:\n  Category == with\n  aa: () -> ()\nBar(X: String): with == add\n");
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    private PsiElement parseText(CharSequence text) {
        return parseText(text, AldorTypes.TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        System.out.println(LexerFunctions.tokens(text));
        return ParserFunctions.parseAldorText(getProject(), text, elementType);
    }

}
