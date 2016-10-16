package aldor.lexer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("MagicNumber")
public class IndentWidthCalculatorTest {

    @Test
    public void testWidth() {
        IndentWidthCalculator calc = new IndentWidthCalculator();
        assertEquals(8, calc.width("\t"));
        assertEquals(8, calc.width(" \t"));
        assertEquals(8, calc.width("  \t"));
        assertEquals(9, calc.width("\t "));
        assertEquals(9, calc.width(" \t "));
    }

    @Test
    public void testOffset() {
        IndentWidthCalculator calc = new IndentWidthCalculator();
        assertEquals(1, calc.offsetForWidth("\tXYZ", 9));
        assertEquals(0, calc.offsetForWidth("\tX", 8));
        assertEquals(0, calc.offsetForWidth("\tX", 1));
        assertEquals(2, calc.offsetForWidth("\t\tX", 17));
    }

}
