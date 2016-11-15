package aldor.util;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

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
        return SExpression.read(new StringReader(s));
    }
}
