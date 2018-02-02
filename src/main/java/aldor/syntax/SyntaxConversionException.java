package aldor.syntax;

@SuppressWarnings("serial")
class SyntaxConversionException extends RuntimeException{
    public SyntaxConversionException(String msg, Exception e) {
        super(msg, e);
    }
}
