package aldor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SysCmdTest {
    @Test
    public void testParse() {
        SysCmd cmd = SysCmd.parse("#pile");
        assertEquals(SysCmd.SysCommandType.Pile, cmd.type());
        assertEquals(1, cmd.text().size());
    }
}
