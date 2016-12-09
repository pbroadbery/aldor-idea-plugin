package aldor.lexer;

import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.util.List;

import static aldor.lexer.AldorTokenTypes.KW_2EQ;
import static aldor.lexer.AldorTokenTypes.KW_Add;
import static aldor.lexer.AldorTokenTypes.KW_And;
import static aldor.lexer.AldorTokenTypes.KW_Assign;
import static aldor.lexer.AldorTokenTypes.KW_BlkEnd;
import static aldor.lexer.AldorTokenTypes.KW_BlkNext;
import static aldor.lexer.AldorTokenTypes.KW_BlkStart;
import static aldor.lexer.AldorTokenTypes.KW_CBrack;
import static aldor.lexer.AldorTokenTypes.KW_CParen;
import static aldor.lexer.AldorTokenTypes.KW_Colon;
import static aldor.lexer.AldorTokenTypes.KW_Comma;
import static aldor.lexer.AldorTokenTypes.KW_Dollar;
import static aldor.lexer.AldorTokenTypes.KW_Else;
import static aldor.lexer.AldorTokenTypes.KW_If;
import static aldor.lexer.AldorTokenTypes.KW_Implies;
import static aldor.lexer.AldorTokenTypes.KW_Indent;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.KW_Not;
import static aldor.lexer.AldorTokenTypes.KW_OBrack;
import static aldor.lexer.AldorTokenTypes.KW_OParen;
import static aldor.lexer.AldorTokenTypes.KW_Plus;
import static aldor.lexer.AldorTokenTypes.KW_Repeat;
import static aldor.lexer.AldorTokenTypes.KW_Return;
import static aldor.lexer.AldorTokenTypes.KW_StartPile;
import static aldor.lexer.AldorTokenTypes.KW_Then;
import static aldor.lexer.AldorTokenTypes.KW_Where;
import static aldor.lexer.AldorTokenTypes.KW_With;
import static aldor.lexer.AldorTokenTypes.TK_Comment;
import static aldor.lexer.AldorTokenTypes.TK_Id;
import static aldor.lexer.AldorTokenTypes.TK_IfLine;
import static aldor.lexer.AldorTokenTypes.TK_Int;
import static aldor.lexer.AldorTokenTypes.TK_PostDoc;
import static aldor.lexer.AldorTokenTypes.TK_PreDoc;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdAbbrev;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdEndIf;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdIf;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;
import static aldor.lexer.LexMode.Spad;
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
                TK_Id, WHITE_SPACE, KW_2EQ, KW_NewLine,
                KW_Indent, TK_Int, KW_BlkNext,
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
    public void testSingleBlock() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo := 1\nNext\n");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, TK_Int, KW_BlkEnd,
                TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void testTwoBlocks() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter());

        unit.start("#pile\nrepeat\n  foo := 1\nrepeat\n  bar := 1\nNext");
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, TK_Int, KW_BlkEnd,
                KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, TK_Int, KW_BlkEnd,
                TK_Id), tokens);
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
                TK_Id, KW_Assign, KW_NewLine,
                KW_Indent, KW_OBrack, TK_Int, KW_Comma, KW_NewLine,
                KW_Indent, TK_Int, KW_Comma, KW_NewLine,
                KW_Indent, TK_Int, KW_CBrack, KW_BlkNext,
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
        // Currently a bit broken.. needs to be fixed somehow.
        unit.start("#pile\nFoo:\n  Category == with");

        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_StartPile, KW_NewLine, TK_Id, KW_Colon, KW_NewLine,
                KW_Indent, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_With
        ), tokens);
    }

    @Test
    public void spadLexerAdd2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "X: Y == Foo add\n   b: X\nE\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_Add, KW_BlkStart,
                KW_Indent, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, KW_BlkEnd,
                TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerAdd() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "X: Y == Foo\n  add\n   b: X\nE\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Indent, KW_Add, KW_BlkStart,
                KW_Indent, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, KW_BlkEnd,
                TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerWith() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "with\n  foo: %\n  ++ foo\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_With, KW_BlkStart,
                KW_Indent, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Indent, TK_PostDoc,
                KW_NewLine), tokens);
    }

    @Test
    public void spadLexerWith2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "with\n  foo: %\n  ++ foo\n  bar: %\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(KW_With, KW_BlkStart,
                KW_Indent, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Indent, TK_PostDoc, KW_BlkNext,
                KW_Indent, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerDecl5() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "++ Some comment\nFoo: with == add\n++ More comment\nB == 2\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, KW_With, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_BlkNext,
                TK_PreDoc, KW_NewLine, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Int, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerDecl() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "++ foo\nFoo: X == Y\n++bar\nBar: A == B\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Id, KW_BlkNext,
                TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerDecl2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "++ foo\nFoo: X == add\n++bar\nBar: A == add\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_BlkNext,
                TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_NewLine), tokens);
    }


    @Test
    public void spadLexerDecl3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "++ Foo\n++More\nFoo: with == add\n++ Bar\n++ More Bar\nBar: with == add\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_PreDoc, KW_NewLine,
                TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, KW_With, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_BlkNext,
                TK_PreDoc, KW_NewLine,
                TK_PreDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, KW_With, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_NewLine), tokens);
    }
    @Test
    public void spadLexerDecl4() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "\n" +
                " IntegerMod():\n" +
                "   Join(StepThrough) == add\n" +
                "    size() == p\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_NewLine, KW_Indent, TK_Id, KW_OParen, KW_CParen, KW_Colon, KW_NewLine,
                KW_Indent, TK_Id, KW_OParen, TK_Id, KW_CParen, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_BlkStart,
                KW_Indent, TK_Id, KW_OParen, KW_CParen, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Id, KW_NewLine
                ), tokens);
    }

    @Test
    public void spadLexerLongExpr() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "add\n  x := [foo\n   ]$List\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_Add, KW_BlkStart,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, KW_OBrack, TK_Id, KW_NewLine,
                KW_Indent, KW_CBrack, KW_Dollar, TK_Id, KW_NewLine
                ), tokens);
    }


    @Test
    public void spadLexerIfLayout() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "if X\n then\n  foo\n else\n  bar\nZZZ\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_If, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Indent, KW_Then, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Indent, KW_Else, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id, KW_NewLine
        ), tokens);
    }


    @Test
    public void spadLexerIfLayout2() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "if X\nthen\n  foo\nelse\n bar\nZZZ\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_If, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Then, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Else, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_Id, KW_NewLine
        ), tokens);
    }


    @Test
    public void spadLexerIfLayout3() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "if X\nthen\n  foo\n  qq\nelse bar\nZZZ\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_If, WHITE_SPACE, TK_Id, KW_NewLine,
                KW_Then, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Else, WHITE_SPACE, TK_Id, KW_BlkNext,
                TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void spadLexerNotBlock() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "foo :=\n  not X => 12\n  repeat\n    Y\n  qq\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, WHITE_SPACE, KW_Assign, KW_BlkStart,
                KW_Indent, KW_Not, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_Implies, WHITE_SPACE, TK_Int, KW_BlkNext,
                KW_Indent, KW_Repeat, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkEnd,
                KW_Indent, TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void spadLexerInfixStart() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "a := foo \n  + bar\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_NewLine,
                KW_Indent, KW_Plus, WHITE_SPACE, TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void spadLexerInfixEnd() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "if R and\n foo then\n  Y\n  Z\nN\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_If, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_And, KW_NewLine,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Then, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id,  KW_BlkEnd,
                TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void spadLexerDubiousDoc() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "X where\n  ++ foo\n  A\n  B\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, WHITE_SPACE, KW_Where, KW_NewLine,
                KW_Indent, TK_PreDoc, KW_BlkStart,
                KW_Indent, TK_Id, KW_BlkNext,
                KW_Indent, TK_Id, KW_NewLine
        ), tokens);
    }


    @Test
    public void spadLexerCommentsInMid() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "if X and \n  B then\n -- foo\n x:=1\n y:=2\nQ\n";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                KW_If, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_And, WHITE_SPACE, KW_NewLine,
                KW_Indent, TK_Id, WHITE_SPACE, KW_Then, KW_BlkStart,
                KW_Indent, TK_Comment, KW_NewLine,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkNext,
                KW_Indent, TK_Id, KW_Assign, TK_Int, KW_BlkEnd,
                TK_Id, KW_NewLine
        ), tokens);
    }

    @Test
    public void spadAbbrevAfterDef() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = "Foo == add\n  blah\n    blah\n)abbrev domain BBB CCC\nBBB == 2";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(
                TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_BlkStart,
                KW_Indent, TK_Id, KW_NewLine,
                KW_Indent, TK_Id, KW_BlkEnd,
                TK_SysCmdAbbrev, KW_NewLine,
                TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Int
        ), tokens);
    }

    @Test
    public void spadShortFile() {
        AldorIndentLexer unit = new AldorIndentLexer(new AldorLexerAdapter(Spad, null));
        String text = ")abbrev foo foo";
        unit.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(unit);
        assertEquals(Lists.newArrayList(TK_SysCmdAbbrev), tokens);

    }
}
