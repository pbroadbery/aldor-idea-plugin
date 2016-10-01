package aldor.util.sexpr;

@SuppressWarnings("serial")
public class SExpressionReadException extends RuntimeException {
    public SExpressionReadException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SExpressionReadException(String msg) {
        super(msg);
    }

}
