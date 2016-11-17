package aldor.parser;

import aldor.lexer.LexerFunctions;
import aldor.psi.elements.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static aldor.psi.AldorPsiUtils.logPsi;

public class PileModeParsingTests2 extends LightPlatformTestCase {

    public void testPiledOne() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nA\n");

        assertEquals(0, errors.size());
}

    public void testPiledTwo() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nA\nB\n");

        assertEquals(0, errors.size());
    }

    public void testPiledThree() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nA\nB\nC\n");

        assertEquals(0, errors.size());
    }

    public void testPiledNoEolAtEof() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nA\nB\nC");

        assertEquals(0, errors.size());

    }

    public void testPiledBlockEolAtEof() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Foo\n Bar\n Baz\n");

        assertEquals(0, errors.size());

    }

    public void testPiledBlockNoEolAtEof() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Foo\n Bar\n Baz");

        assertEquals(0, errors.size());

    }

    public void testPiledBlockThenMoreStuff() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Foo\nBar\n");

        assertEquals(0, errors.size());

    }


    public void testPiledBlockPiledBlock() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Foo\nrepeat\n Bar\n");

        assertEquals(0, errors.size());

    }


    public void testNestedPiledBlock() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Loop1\n repeat\n  Bar\n Loop2\nLast");

        assertEquals(0, errors.size());

    }

    public void testNestedPiledBlockExit2() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Loop1\n repeat\n  Bar\nLast");

        assertEquals(0, errors.size());
    }


    public void testNestedPiledBlockExitAll() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Loop1\n repeat\n  Bar\n");

        assertEquals(0, errors.size());
    }

    public void testNestedPiledBlockExitAllNoEolAtEof() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\nrepeat\n Loop1\n repeat\n  Bar");

        assertEquals(0, errors.size());
    }

    public void testNestedTwoDefnsTopLevel() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");

        assertEquals(0, errors.size());
    }


    public void testTopLevelMixedMode() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "Foo := 1;\n" +
                "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");

        assertEquals(0, errors.size());
    }


    public void testTopLevelMixedMode2() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL, "Foo := 1\n" +
                "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");

        assertEquals(0, errors.size());
    }

    public void testIfSysCmd() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL,
                "Foo := 1\n" +
                "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");

        assertEquals(0, errors.size());
    }


    public void testPileSysCmdIf() {
        final List<PsiErrorElement> errors = parseForErrors(AldorTypes.TOP_LEVEL,
                "#pile\nX\n#if NOPE\n\n#endif\nY\n");

        assertEquals(0, errors.size());
    }


    @NotNull
    private List<PsiErrorElement> parseForErrors(IElementType type, CharSequence text) {
        System.out.println("Tokens are: " + LexerFunctions.tokens(text));
        PsiElement psi = parseText(text, type);
        logPsi(psi);
        return ParserFunctions.getPsiErrorElements(psi);
    }


    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseAldorText(getProject(), text, elementType);
    }


}
