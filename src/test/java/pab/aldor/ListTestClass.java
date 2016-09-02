package pab.aldor;

import aldor.list.ListLexerAdapter;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for List - Barely tests anything, but "List" is just a toy language
 */
public class ListTestClass  {

    @Test
    public void canParseList() {
        ListLexerAdapter lla = new ListLexerAdapter(new StringReader("["));
        lla.start("1,2,3");
        System.out.println("Pos: " + lla.getCurrentPosition().getState() + " " + lla.getBufferEnd() + " " + lla.getTokenType());

        lla.advance();
        assertNotNull(lla.getTokenType());
    }

}
