package aldor.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class InstanceCounterTest {

    @Test
    public void test() {
        assertEquals(0, new Wibble().getInstanceId());
        assertEquals(1, new Wibble().getInstanceId());
    }

    private static class Wibble {
        private final int instanceId = InstanceCounter.instance().next(Wibble.class);

        public int getInstanceId() {
            return instanceId;
        }
    }


}