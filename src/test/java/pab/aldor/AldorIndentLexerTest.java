package pab.aldor;

import aldor.AldorIndentLexer;
import aldor.AldorLexerAdapter;
import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.util.List;

import static aldor.AldorTokenTypes.*;
import static org.junit.Assert.assertEquals;

public class AldorIndentLexerTest {

    @Test
    public void testNoIndent() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("\nWords\n");

        assertEquals(Lists.newArrayList(KW_NewLine, TK_Id, KW_NewLine), LexerFunctions.readTokens(unit));
    }


    @Test
    public void testIndent() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo:= 2\nBlah");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_SysCmd, KW_NewLine,
                KW_Repeat, KW_NewLine,
                KW_SetTab, TK_Id, KW_Assign, WHITE_SPACE, TK_Int, KW_NewLine,
                TK_Id), tokens);
     }


    @Test
    public void testIndent2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo\n  Blah\nLast");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_SysCmd, KW_NewLine,
                KW_Repeat, KW_NewLine,
                KW_SetTab, TK_Id, KW_NewLine,
                KW_BackSet, TK_Id, KW_NewLine,
                TK_Id), tokens);
    }


    @Test
    public void testIndent2Eq() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFoo ==\n First\n Second\nThird");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_SysCmd, KW_NewLine,
                TK_Id, WHITE_SPACE, KW_2EQ, KW_NewLine,
                KW_SetTab, TK_Id, KW_NewLine,
                KW_BackSet, TK_Id, KW_NewLine,
                TK_Id), tokens);
    }

    @Test
    public void testNestedBlocks() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n repeat1 :=\n  assign1\n  assign2\n repeat2");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_SysCmd, KW_NewLine,
                KW_Repeat, KW_NewLine,
                KW_SetTab, TK_Id, WHITE_SPACE, KW_Assign, KW_NewLine,
                KW_SetTab, TK_Id, KW_NewLine,
                KW_BackSet, TK_Id, KW_NewLine,
                KW_BackSet, TK_Id), tokens);

    }


}
