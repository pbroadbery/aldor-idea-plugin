package aldor.util.sexpr;

import aldor.util.sexpr.impl.SExpressionReader;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static aldor.util.sexpr.SymbolPolicy.NORMAL;

public final class SExpressions {

    @SuppressWarnings("OverloadedVarargsMethod")
    public static SExpression list(SExpression ... sxArray) {
        return list(Arrays.asList(sxArray));
    }

    private static SExpression list(List<SExpression> ts) {
        SExpression whole = SExpression.nil();
        for (int i=ts.size()-1; i>= 0; i--) {
            whole = SExpression.cons(ts.get(i), whole);
        }
        return whole;
    }

    public static SExpression readFromString(String s) {
        return read(new StringReader(s));
    }

    public static SExpression read(Reader reader, SymbolPolicy symbolPolicy) {
        SExpressionReader rdr = new SExpressionReader(reader, symbolPolicy);
        return rdr.read();
    }

    public static SExpression read(Reader reader) {
        return read(reader, NORMAL);
    }

}
