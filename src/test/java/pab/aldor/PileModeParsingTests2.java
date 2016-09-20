package pab.aldor;

import aldor.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformTestCase;

import java.util.List;

import static pab.aldor.ParserFunctions.getPsiErrorElements;
import static pab.aldor.ParserFunctions.logPsi;

public class PileModeParsingTests2 extends LightPlatformTestCase {

    public void testPiledOne() {
        checkNoErrors("#pile\nA\n");
    }

    public void testPiledTwo() {
        checkNoErrors("#pile\nA\nB\n");
    }

    public void testPiledThree() {
        checkNoErrors("#pile\nA\nB\nC\n");
    }

    public void testPiledNoEolAtEof() {
        checkNoErrors("#pile\nA\nB\nC");
    }

    public void testPiledBlockEolAtEof() {
        checkNoErrors("#pile\nrepeat\n Foo\n Bar\n Baz\n");
    }

    public void testPiledBlockNoEolAtEof() {
        checkNoErrors("#pile\nrepeat\n Foo\n Bar\n Baz");
    }

    public void testPiledBlockThenMoreStuff() {
        checkNoErrors("#pile\nrepeat\n Foo\nBar\n");
    }


    public void testPiledBlockPiledBlock() {
        checkNoErrors("#pile\nrepeat\n Foo\nrepeat\n Bar\n");
    }


    public void testNestedPiledBlock() {
        checkNoErrors("#pile\nrepeat\n Loop1\n repeat\n  Bar\n Loop2\nLast");
    }

    public void testNestedPiledBlockExit2() {
        checkNoErrors("#pile\nrepeat\n Loop1\n repeat\n  Bar\nLast");
    }


    public void testNestedPiledBlockExitAll() {
        checkNoErrors("#pile\nrepeat\n Loop1\n repeat\n  Bar\n");
    }

    public void testNestedPiledBlockExitAllNoEolAtEof() {
        checkNoErrors("#pile\nrepeat\n Loop1\n repeat\n  Bar");
    }

    public void testNestedTwoDefnsTopLevel() {
        checkNoErrors("#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");
    }


    public void testTopLevelMixedMode() {
        checkNoErrors(AldorTypes.TOP_LEVEL,
                "Foo := 1;\n" +
                "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");
    }


    public void testTopLevelMixedMode2() {
        checkNoErrors(AldorTypes.TOP_LEVEL,
                "Foo := 1\n" +
                "#pile\n" +
                "\n" +
                "Foo: X ==  1\n" +
                "QQ: Y == 2\n");
    }


    private void checkNoErrors(IElementType type, String text) {
        System.out.println("Tokens are: " + LexerFunctions.tokens(text));
        PsiElement psi = parseText(text, type);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);

        assertEquals(0, errors.size());

    }


    private void checkNoErrors(String text) {
        checkNoErrors(AldorTypes.PILED_CONTENT, text);
     }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseText(getProject(), text, elementType);
    }


}
