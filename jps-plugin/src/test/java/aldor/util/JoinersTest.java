package aldor.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JoinersTest {

    @Test
    public void testJoiner1() {
        String ll = Joiners.truncate(1, Arrays.asList("line1", "line2", "line3"));
        assertEquals("line1+...", ll.replace('\n', '+'));
    }

    @Test
    public void testJoiner2() {
        String ll = Joiners.truncate(2, Arrays.asList("line1", "line2", "line3"));
        assertEquals("line1+line2+...", ll.replace('\n', '+'));
    }

}
