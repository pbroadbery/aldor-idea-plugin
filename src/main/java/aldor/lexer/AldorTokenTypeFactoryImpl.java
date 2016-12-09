package aldor.lexer;

import aldor.language.AldorLanguage;
import com.intellij.psi.tree.IElementType;

public class AldorTokenTypeFactoryImpl implements AldorTokenTypeFactory {

    public AldorTokenTypeFactoryImpl() {
    }

    @Override
    public AldorTokenType createTokenType(String name, AldorTokenAttributes aldorTokenAttributes) {
        return new AldorTokenType(name, aldorTokenAttributes.getI(), aldorTokenAttributes.getText(), aldorTokenAttributes.getHasString(), aldorTokenAttributes.getIsComment(), aldorTokenAttributes.getIsOpener(), aldorTokenAttributes.getIsCloser(), aldorTokenAttributes.getIsFollower(), aldorTokenAttributes.getIsLangword(), aldorTokenAttributes.getIsLeftAssoc(), aldorTokenAttributes.getIsMaybeInfix(), aldorTokenAttributes.getPrecedence(), aldorTokenAttributes.getIsDisabled());
    }

    @Override
    public IElementType createSysCmdTokenType(String name) {
        return new IElementType(name, AldorLanguage.INSTANCE);
    }
}
