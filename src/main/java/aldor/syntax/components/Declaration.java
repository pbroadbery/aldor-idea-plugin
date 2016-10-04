package aldor.syntax.components;

import aldor.psi.AldorDeclPart;
import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by pab on 04/10/16.
 */
public class Declaration extends SyntaxNode<AldorDeclPart> {
    public Declaration(AldorDeclPart element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
    }

    public Syntax lhs() {
        return child(0);
    }

    public Syntax rhs() {
        return child(1);
    }

    @Override
    public String name() {
        return "Decl";
    }
}
