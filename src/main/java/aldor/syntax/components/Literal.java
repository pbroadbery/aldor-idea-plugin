package aldor.syntax.components;

import aldor.psi.AldorLiteral;
import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;

import java.util.Collections;

/**
 * Created by pab on 04/10/16.
 */
public class Literal extends Syntax {
    private final String text;
    private final AldorLiteral literal;

    public Literal(String text, AldorLiteral literal) {
        this.text = text;
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
}
