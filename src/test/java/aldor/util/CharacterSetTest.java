package aldor.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CharacterSetTest {

    @Test
    public void testSimple() {
        CharacterSet set = CharacterSet.create(Arrays.asList('a', 'b', 'a'));
        assertEquals(2, set.size());
        assertTrue(set.contains('a'));
        assertTrue(set.contains('b'));
        //noinspection ReplaceInefficientStreamCount
        assertEquals(2, set.stream().count());
    }

}
