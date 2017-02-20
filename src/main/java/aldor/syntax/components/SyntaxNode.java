package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.syntax.Syntax;
import com.google.common.base.Joiner;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Standard-ish abstract class for syntax.
 */
public abstract class SyntaxNode<SyntaxPsiElement extends PsiElement> extends Syntax {
    protected final List<Syntax> arguments;
    private final SyntaxRepresentation<SyntaxPsiElement> representation;

    protected SyntaxNode(SyntaxPsiElement element, List<Syntax> arguments) {
        this.representation = SyntaxRepresentation.create(element);
        this.arguments = new ArrayList<>(arguments);
        if (arguments.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found a null: " + arguments);
        }
    }


    protected SyntaxNode(SyntaxRepresentation<SyntaxPsiElement> rep, List<Syntax> arguments) {
        this.representation = rep;
        this.arguments = new ArrayList<>(arguments);
        if (arguments.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found a null: " + arguments);
        }
    }


    @Override
    public String toString() {
        return "(" + name() + " " + Joiner.on(" ").join(arguments) + ")";
    }

    @Override
    public SyntaxPsiElement psiElement() {
        return representation.element();
    }

    @Override
    public List<Syntax> children() {
        return Collections.unmodifiableList(arguments);
    }

    public Syntax child(int n) {
        return arguments.get(n);
    }

    @Nullable
    @Override
    public AldorTokenType tokenType() {
        return null;
    }
}
