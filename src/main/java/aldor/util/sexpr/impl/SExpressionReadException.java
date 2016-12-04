package aldor.util.sexpr.impl;

@SuppressWarnings("serial")
public class SExpressionReadException extends RuntimeException {
    public SExpressionReadException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SExpressionReadException(String msg) {
        super(msg);
    }

}
