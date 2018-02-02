package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorLiteral;
import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Literal (numbers, strings, ...)
 */
public class Literal extends Syntax {
    private final AldorLiteral literal;
    private final String text;

    public Literal(String text, AldorLiteral literal) {
        this.literal = literal;
        this.text = text;
    }

    @Override
    public String name() {
        return "Literal";
    }

    @Override
    public PsiElement psiElement() {
        return literal;
    }

    @Override
    public Collection<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return text();
    }

    @Nullable
    @Override
    public AldorTokenType tokenType() {
        if (literal.getTKInt() != null) {
            return AldorTokenTypes.TK_Int;
        }
        if (literal.getTKFloat() != null) {
            return AldorTokenTypes.TK_Float;
        }
        if (literal.getTKString() != null) {
            return AldorTokenTypes.TK_String;
        }
        return null;
    }

    public String text() {
        return text;
    }
}
