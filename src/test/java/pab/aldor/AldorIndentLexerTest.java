package pab.aldor;

import aldor.AldorIndentLexer;
import aldor.AldorLexerAdapter;
import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.util.List;

import static aldor.AldorTokenTypes.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AldorIndentLexerTest {

    @Test
    public void testNoIndent() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("\nBunch of stuff\t\n");

        assertTrue(unit.indentMap().isEmpty());
    }


    @Test
    public void testNotMuchIndentAtAll() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("\n");

        assertTrue(unit.indentMap().isEmpty());
    }


    @Test
    public void testIndent() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("Hello\n  There\n MoreText");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_Id, WHITE_SPACE,
                                        KW_Indent, TK_Id, WHITE_SPACE,
                                        KW_Indent, TK_Id), tokens);
        System.out.println("Tokens: " + tokens);
        System.out.println("Widths: " + unit.indentMap());
        assertEquals(2, (int) unit.indentMap().get(6));
    }



}
