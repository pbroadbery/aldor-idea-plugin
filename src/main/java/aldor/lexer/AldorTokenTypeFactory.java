package aldor.lexer;

public interface AldorTokenTypeFactory {
    static AldorTokenTypeFactory instance = new AldorTokenTypeFactoryImpl();

    public AldorTokenType createToken(String name, AldorTokenAttributes attributes);
}
