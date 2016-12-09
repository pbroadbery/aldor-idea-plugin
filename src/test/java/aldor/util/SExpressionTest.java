package aldor.util;

import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SymbolPolicy;
import aldor.util.sexpr.impl.SExpressionReadException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SExpressionTest {

    @Test
    public void testReadPolicy() throws FileNotFoundException {
        SExpression sx = SExpression.read(new StringReader("foo"), SymbolPolicy.ALLCAPS);
        assertEquals(sx, SExpression.symbol("FOO"));
    }

    @Test
    public void testReadNice() throws FileNotFoundException {
        SExpression sx = SExpression.read(new StringReader("(Hello there)"));
        assertEquals(sx, SExpression.cons(SExpression.symbol("Hello"),
                SExpression.cons(SExpression.symbol("there"), SExpression.nil())));
    }

    @Test
    public void testReadIncomplete() throws FileNotFoundException {
        try {
            @SuppressWarnings("UnusedAssignment") SExpression sx = SExpression.read(new StringReader("(Hello there"));
            fail("Expected an exception");
        }
        catch (SExpressionReadException ignored) {

        }
    }


    @Test
    public void testList() {
        SExpression sx = SExpression.read(new StringReader("(1 2 3 4)"));
        assertEquals(SExpression.integer(1), sx.asList().get(0));
        assertEquals(SExpression.integer(2), sx.asList().get(1));
        assertEquals(SExpression.integer(3), sx.asList().get(2));
        assertEquals(SExpression.integer(4), sx.asList().get(3));
        assertEquals(4, sx.asList().size());
    }

}
