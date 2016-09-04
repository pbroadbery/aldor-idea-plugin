package pab.aldor;

import aldor.AldorLexerAdapter;
import com.google.common.collect.Lists;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static aldor.AldorTokenTypes.*;
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
        System.out.println("Pos: " + lla.getCurrentPosition().getState() + " " + lla.getBufferEnd() + " " + lla.getTokenType());
        assertEquals(KW_Add, lla.getTokenType());
        lla.advance();
        System.out.println(lla.getTokenType());
        assertNull(lla.getTokenType());
    }

    @Test
    public void canParseNumber() {
        AldorLexerAdapter lla = new AldorLexerAdapter(new StringReader("12"));
        lla.start("12");
        System.out.println("Pos: " + lla.getCurrentPosition().getState() + " " + lla.getBufferEnd() + " " + lla.getTokenType());
        assertEquals(TK_Int, lla.getTokenType());
        lla.advance();
        System.out.println(lla.getTokenType());
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
        assertEquals(Lists.newArrayList(KW_NewLine, TK_SysCmd, KW_NewLine), tokens);
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


}
