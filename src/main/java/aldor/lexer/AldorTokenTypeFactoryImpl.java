package aldor.lexer;

public class AldorTokenTypeFactoryImpl implements AldorTokenTypeFactory {

    public AldorTokenTypeFactoryImpl() {
     }

    @Override
    public AldorTokenType createToken(String name, AldorTokenAttributes aldorTokenAttributes) {
        return new AldorTokenType(name, aldorTokenAttributes.getI(), aldorTokenAttributes.getText(), aldorTokenAttributes.getHasString(), aldorTokenAttributes.getIsComment(), aldorTokenAttributes.getIsOpener(), aldorTokenAttributes.getIsCloser(), aldorTokenAttributes.getIsFollower(), aldorTokenAttributes.getIsLangword(), aldorTokenAttributes.getIsLeftAssoc(), aldorTokenAttributes.getIsMaybeInfix(), aldorTokenAttributes.getPrecedence(), aldorTokenAttributes.getIsDisabled());
    }
}
