package aldor;

import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.util.List;

import static aldor.lexer.AldorTokenTypes.KW_2EQ;
import static aldor.lexer.AldorTokenTypes.KW_Assign;
import static aldor.lexer.AldorTokenTypes.KW_BlkEnd;
import static aldor.lexer.AldorTokenTypes.KW_BlkNext;
import static aldor.lexer.AldorTokenTypes.KW_BlkStart;
import static aldor.lexer.AldorTokenTypes.KW_CBrack;
import static aldor.lexer.AldorTokenTypes.KW_Comma;
import static aldor.lexer.AldorTokenTypes.KW_Else;
import static aldor.lexer.AldorTokenTypes.KW_If;
import static aldor.lexer.AldorTokenTypes.KW_Indent;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.KW_OBrack;
import static aldor.lexer.AldorTokenTypes.KW_Repeat;
import static aldor.lexer.AldorTokenTypes.KW_Return;
import static aldor.lexer.AldorTokenTypes.KW_StartPile;
import static aldor.lexer.AldorTokenTypes.KW_Then;
import static aldor.lexer.AldorTokenTypes.TK_Comment;
import static aldor.lexer.AldorTokenTypes.TK_Id;
import static aldor.lexer.AldorTokenTypes.TK_IfLine;
import static aldor.lexer.AldorTokenTypes.TK_Int;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdEndIf;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdIf;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;
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

        unit.start("#pile\nrepeat\n  foo:=2\nBlah");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                TK_Id), tokens);
     }

    @Test
    public void testTopLevel() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFirstLine\nNextLine\n");
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_BlkNext,
                TK_Id, KW_NewLine), LexerFunctions.readTokens(unit));
    }

    @Test
    public void testTopLevel2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFirstLine\nNextLine");

        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_BlkNext,
                TK_Id), LexerFunctions.readTokens(unit));
    }

    @Test
    public void testTopLevel3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFirstLine ==\n  1\n\nNextLine == 2");

        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, WHITE_SPACE, KW_2EQ, KW_BlkStart,
                KW_Indent, TK_Int, KW_BlkEnd,
                KW_NewLine,
                TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Int), LexerFunctions.readTokens(unit));
    }



    @Test
    public void testIndent2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo\n  Blah\nLast");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id), tokens);
    }


    @Test
    public void testIndent4() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  x:=1\n  y:=2");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkNext,
                KW_Indent, TK_Id, KW_Assign, TK_Int),
                tokens);
    }

    @Test
    public void testIndent5() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  x:=1\n  y:=2\nLast");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkNext,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                TK_Id),
                tokens);
    }

    @Test
    public void testIndentBlankLine() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  A\n\n  B\nLast");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_NewLine,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id),
                tokens);
    }

    @Test
    public void testNestedIndent1() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n repeat\n  x:=1");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int), tokens);
    }


    @Test
    public void testIncomplete() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  \n");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_NewLine, // BlkStart, surely?
                KW_Indent, KW_NewLine), tokens);
    }

    @Test
    public void testNestedIndent2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n repeat\n  x:=1\n  y:=2\nx");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkNext,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                TK_Id), tokens);
    }

    @Test
    public void testNestedIndent3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n repeat\n  x:=1\n y:=2\nx");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                TK_Id), tokens);
    }


    @Test
    public void testIndent3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo\nrepeat\n  bar\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_NewLine), tokens);
    }



    @Test
    public void testIndent2Eq() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFoo ==\n First\n Second\nThird");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, WHITE_SPACE, KW_2EQ, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id), tokens);
    }

    @Test
    public void testNestedBlocks() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n repeat1 :=\n  assign1\n  assign2\n repeat2");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Assign, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Indent, TK_Id), tokens);

    }

    @Test
    public void testContinuationLine() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nqq:=[1,\n2]\npp\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
            TK_Id, KW_Assign, KW_OBrack, TK_Int, KW_Comma, KW_NewLine,
            TK_Int, KW_CBrack, KW_BlkNext,
            TK_Id, KW_NewLine), tokens);

    }

    @Test
    public void testContinuationLine2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nqq:=[1,\n2,\n3]\npp\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_Assign, KW_OBrack, TK_Int, KW_Comma, KW_NewLine,
                TK_Int, KW_Comma, KW_NewLine,
                TK_Int, KW_CBrack, KW_BlkNext,
                TK_Id, KW_NewLine), tokens);

    }


    @Test
    public void testContinuationLine3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nqq:=\n  [1,\n   2,\n   3]\npp\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_Assign, KW_BlkStart,
                KW_Indent, KW_OBrack, TK_Int, KW_Comma, KW_NewLine,
                KW_Indent, TK_Int, KW_Comma, KW_NewLine,
                KW_Indent, TK_Int, KW_CBrack, KW_BlkEnd,
                TK_Id, KW_NewLine), tokens);
    }


    @Test
    public void testIfStatement() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nif X then\n    A\nelse\n    B\nX\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_If, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_Then, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Else, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void testIfElseStatement() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\n" +
                "if X then A\n" +
                "else if Y then B\n" +
                "else C\n" +
                "Y");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_If, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_Then, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Else, WHITE_SPACE, KW_If, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_Then, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Else, WHITE_SPACE, TK_Id, KW_BlkNext, TK_Id),
                     tokens);
    }



    @Test
    public void testPiledIfSysCmd() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nX\n#if NOPE\nA\n#endif\nY\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_BlkNext,
                TK_SysCmdIf, KW_NewLine,
                TK_IfLine, KW_NewLine,
                TK_SysCmdEndIf, KW_NewLine,
                TK_Id, KW_NewLine),
                tokens);
    }

    @Test
    public void testComments() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\n-- this is a fish\nFoo:=Bar\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Comment, KW_NewLine, TK_Id, KW_Assign, TK_Id, KW_NewLine
               ), tokens);

    }


    @Test
    public void testNestedEnds() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nf==\n repeat\n  L1\n return X\n\nQ:=2\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                TK_Id, KW_2EQ, KW_BlkStart,
                KW_Indent, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Indent, KW_Return, WHITE_SPACE, TK_Id, KW_BlkEnd,
                KW_NewLine,
                TK_Id, KW_Assign, TK_Int, KW_NewLine), tokens);

    }

    @Test
    public void testLeadingWhitespace() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\n\nFoo:=Bar\n");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_NewLine,
                TK_Id, KW_Assign, TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void testPiledDeclaration() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nFoo:\n  Category == with");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine
        ), tokens);
    }


}
