package aldor.syntax.components;

import aldor.psi.AldorParened;
import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * (1,2,3)
 */
public class Comma extends SyntaxNode<AldorParened> {
    public Comma(AldorParened element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
    }

    @Override
    public String name() {
        return "Comma";
    }
}
