package aldor.lexer;

import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static aldor.lexer.AldorTokenTypes.KW_2EQ;
import static aldor.lexer.AldorTokenTypes.KW_Add;
import static aldor.lexer.AldorTokenTypes.KW_Assign;
import static aldor.lexer.AldorTokenTypes.KW_Colon;
import static aldor.lexer.AldorTokenTypes.KW_Define;
import static aldor.lexer.AldorTokenTypes.KW_Indent;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.KW_Quote;
import static aldor.lexer.AldorTokenTypes.KW_Repeat;
import static aldor.lexer.AldorTokenTypes.KW_With;
import static aldor.lexer.AldorTokenTypes.TK_Id;
import static aldor.lexer.AldorTokenTypes.TK_IfLine;
import static aldor.lexer.AldorTokenTypes.TK_Int;
import static aldor.lexer.AldorTokenTypes.TK_PostDoc;
import static aldor.lexer.AldorTokenTypes.TK_String;
import static aldor.lexer.AldorTokenTypes.TK_SysCmd;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdAbbrev;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdAssert;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdEndIf;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdId;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdIf;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdInclude;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdLibrary;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdPrefix;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdString;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdWS;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;
import static aldor.lexer.LexMode.Aldor;
import static aldor.lexer.LexMode.Spad;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Text fun things about lexers
 */
public class AldorLexerTest {
    @Test
    public void canParseKeyword() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("["));
        lla.start("add");
        assertEquals(KW_Add, lla.getTokenType());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseNumber() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("12");
        assertEquals(TK_Int, lla.getTokenType());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseString() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("\"foo\"");
        assertEquals(TK_String, lla.getTokenType());
        assertEquals("\"foo\"", lla.getTokenText());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseEscapedString() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("\"_\" \"");
        assertEquals(TK_String, lla.getTokenType());
        assertEquals("\"_\" \"", lla.getTokenText());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseEscapedEscapedString() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("\"__|__\"");
        assertEquals(TK_String, lla.getTokenType());
        assertEquals("\"__|__\"", lla.getTokenText());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseEscapedNewlineString() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("\"foo _\nbar\"");
        assertEquals(TK_String, lla.getTokenType());
        assertEquals("\"foo _\nbar\"", lla.getTokenText());
        lla.advance();
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseDefinition() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("define foo: X == 12"));
        lla.start("define foo: X == 12");
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(KW_Define, WHITE_SPACE, TK_Id, KW_Colon, WHITE_SPACE, TK_Id, WHITE_SPACE, KW_2EQ, WHITE_SPACE, TK_Int), tokens);
    }


    @Test
    public void canParseSysCommands() {
        AldorLexerAdapter lla = new AldorLexerAdapter(null);
        lla.start("\n#foo\n");
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(KW_NewLine, TK_SysCmdPrefix, TK_SysCmdId, KW_NewLine), tokens);
    }


    @Test
    public void newlines() {
        AldorLexerAdapter lla = new AldorLexerAdapter(null);
        lla.start("f\ng");
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_Id, KW_NewLine, TK_Id), tokens);
    }

    @Test
    public void indentedLoop() {
        AldorLexerAdapter lla = new AldorLexerAdapter(null);
        String text = "repeat\n  fx\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(KW_Repeat, KW_NewLine, KW_Indent, TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void ifScans() {
        AldorLexerAdapter lla = new AldorLexerAdapter(null);
        String text = "Foo\n#if XYZ\nRandomness%^*\n#endif\nStuff\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_Id, KW_NewLine, TK_SysCmdIf, KW_NewLine, TK_IfLine, KW_NewLine, TK_SysCmdEndIf, KW_NewLine, TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerTest() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Spad, null);
        String text = ")abbrev Foo bar\nA: with == add\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_SysCmdAbbrev, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, KW_With, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_NewLine), tokens);
    }

    @Test
    public void spadLexerTest2() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Spad, null);
        String text = ")abbrev Foo bar\n++ Foo\nA: with == add\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_SysCmdAbbrev, KW_NewLine,
                TK_PostDoc, KW_NewLine,
                TK_Id, KW_Colon, WHITE_SPACE, KW_With, WHITE_SPACE, KW_2EQ, WHITE_SPACE, KW_Add, KW_NewLine),
                tokens);
    }

    @Test
    public void spadLexerTestQuoteIds() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Spad, null);
        String text = "foo' := foo''";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_Id, WHITE_SPACE, KW_Assign, WHITE_SPACE, TK_Id), tokens);
    }

    @Test
    public void aldorLexerTestQuoteIds() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Aldor, null);
        String text = "foo'\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_Id, KW_Quote, KW_NewLine), tokens);
    }

    @Test
    public void checkPrefixLexed() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Aldor, null);
        String text = "incremental forth returning selected\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_Id, WHITE_SPACE, TK_Id, WHITE_SPACE, TK_Id, WHITE_SPACE, TK_Id, KW_NewLine), tokens);
    }

    @Test
    public void testSysCmdInclude() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Aldor, null);
        String text = "#include \"foo.as\"\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_SysCmdInclude, KW_NewLine), tokens);
    }

    @Test
    public void testSysCmdLibrary() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Aldor, null);
        String text = "#library Foo \"foo.ao\"\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_SysCmdLibrary, KW_NewLine), tokens);
    }

    @Test
    public void testSysCmdAssert() {
        AldorLexerAdapter lla = new AldorLexerAdapter(Aldor, null);
        String text = "#assert Foo\n";
        lla.start(text);
        List<IElementType> tokens = LexerFunctions.readTokens(lla);
        assertEquals(Lists.newArrayList(TK_SysCmdAssert, KW_NewLine), tokens);
    }
}
