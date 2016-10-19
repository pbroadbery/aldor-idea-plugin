package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.util.SExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class SxSyntaxRepresentation<T extends PsiElement> extends SyntaxRepresentation<T> {
    private final String symbol;
    private final SExpression sx;

    public SxSyntaxRepresentation(SExpression sx, @Nullable String symbol) {
        this.sx = sx;
        this.symbol = symbol;
    }

    @Nullable
    @Override
    public T element() {
        return null;
    }

    @Override
    public AldorTokenType tokenType() {
        return AldorTokenTypes.forText(symbol);
    }

    @Override
    public String text() {
        return symbol;
    }
}
