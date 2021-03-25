package aldor.syntax;

import aldor.lexer.AldorTokenType;
import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.syntax.components.SyntaxRepresentation;
import org.jetbrains.annotations.Nullable;

class TokenSyntax extends SyntaxRepresentation<AldorIdentifier> {

    private final String text;
    private AldorTokenType tokenType;

    public TokenSyntax(String text, AldorTokenType tokenType) {
        this.text = text;
        this.tokenType = tokenType;
    }

    @Nullable
    @Override
    public AldorId element() {
        return null;
    }

    @Nullable
    @Override
    public AldorTokenType tokenType() {
        return tokenType;
    }

    @Override
    public String text() {
        return text;
    }
}
