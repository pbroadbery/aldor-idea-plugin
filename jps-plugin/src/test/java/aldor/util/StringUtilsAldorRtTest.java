package aldor.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsAldorRtTest {

    @Test
    public void testTrimExtension() {
        assertEquals("foo", StringUtilsAldorRt.trimExtension("foo.as"));
        assertEquals("foo", StringUtilsAldorRt.trimExtension("foo"));
    }
}