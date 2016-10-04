package aldor.util;

import aldor.util.sexpr.SExpressionReadException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SExpressionTest {

    @Test
    public void testReadNice() throws FileNotFoundException {
        SExpression sx = SExpression.read(new StringReader("(Hello there)"));
        assertEquals(sx, SExpression.cons(SExpression.symbol("Hello"),
                SExpression.cons(SExpression.symbol("there"), SExpression.nil())));
    }

    @Test
    public void testReadIncomplete() throws FileNotFoundException {
        try {
            SExpression sx = SExpression.read(new StringReader("(Hello there"));
            fail("Expected an exception");
        }
        catch (SExpressionReadException ignored) {

        }
    }
}
