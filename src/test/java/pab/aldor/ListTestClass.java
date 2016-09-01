package pab.aldor;

import aldor.list.ListLexerAdapter;
import org.junit.Test;

import java.io.StringReader;

/**
 * Tests for List
 */
public class ListTestClass  {

    @Test
    public void canParseList() {
        ListLexerAdapter lla = new ListLexerAdapter(new StringReader("["));
        lla.start("1,2,3");
        System.out.println("Pos: " + lla.getCurrentPosition().getState() + " " + lla.getBufferEnd() + " " + lla.getTokenType());

        lla.advance();
        System.out.println(lla.getTokenType());
    }

}
