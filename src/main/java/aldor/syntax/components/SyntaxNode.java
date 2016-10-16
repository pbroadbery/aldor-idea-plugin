package aldor.syntax.components;

import aldor.syntax.Syntax;
import com.google.common.base.Joiner;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standard-ish abstract class for syntax.
 */
public abstract class SyntaxNode<SyntaxPsiElement extends PsiElement> extends Syntax {
    protected final List<Syntax> arguments;
    private final SyntaxPsiElement element;

    protected SyntaxNode(SyntaxPsiElement element, List<Syntax> arguments) {
        this.element = element;
        this.arguments = new ArrayList<>(arguments);
        if (arguments.stream().filter(x->x == null).findAny().isPresent()) {
            throw new IllegalArgumentException("Found a null: " + arguments);
        }
    }

    @Override
    public String toString() {
        return "(" + name() + " " + Joiner.on(" ").join(arguments) + ")";
    }

    @Override
    public SyntaxPsiElement psiElement() {
        return element;
    }

    @Override
    public Iterable<Syntax> children() {
        return Collections.unmodifiableList(arguments);
    }

    public Syntax child(int n) {
        return arguments.get(n);
    }
}
