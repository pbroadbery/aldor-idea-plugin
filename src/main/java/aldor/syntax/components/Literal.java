package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorLiteral;
import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Literal (numbers, strings, ...)
 */
public class Literal extends Syntax {
    private final AldorLiteral literal;

    public Literal(@SuppressWarnings("UnusedParameters") String text, AldorLiteral literal) {
        this.literal = literal;
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
    public Iterable<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return name();
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
}
