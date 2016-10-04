package aldor.syntax.components;

import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by pab on 04/10/16.
 */
public class Apply extends SyntaxNode<PsiElement> {
    public enum ApplyFormat {Normal, Infix;}

    private final ApplyFormat applyFormat;

    public Apply(PsiElement element, ApplyFormat applyFormat, @NotNull List<Syntax> arguments) {
        super(element, arguments);
        this.applyFormat = applyFormat;
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
