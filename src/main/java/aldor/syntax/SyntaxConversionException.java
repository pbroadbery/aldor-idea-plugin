package aldor.syntax;

@SuppressWarnings("serial")
public class SyntaxConversionException extends RuntimeException{
    public SyntaxConversionException(String msg, Exception e) {
        super(msg, e);
    }

    public SyntaxConversionException(String msg) {
        super(msg);
    }
}
