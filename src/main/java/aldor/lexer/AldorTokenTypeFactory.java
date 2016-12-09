package aldor.lexer;

import com.intellij.psi.tree.IElementType;

public interface AldorTokenTypeFactory {

    AldorTokenType createTokenType(String name, AldorTokenAttributes attributes);

    IElementType createSysCmdTokenType(String name);
}
