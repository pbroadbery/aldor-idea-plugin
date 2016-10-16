package aldor.syntax.components;

import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Apply node.
 */
public class Apply extends SyntaxNode<PsiElement> {

    public Apply(PsiElement element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
    }

    public Syntax operator() {
        return arguments.get(0);
    }

    @Override
    public String name() {
        return "Apply";
    }

    public List<Syntax> arguments() {
        return arguments.subList(1, arguments.size());
    }
}
