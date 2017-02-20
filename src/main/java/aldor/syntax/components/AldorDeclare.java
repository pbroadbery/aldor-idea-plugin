package aldor.syntax.components;

import aldor.psi.AldorDeclPart;
import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static aldor.syntax.components.SyntaxRepresentation.createMissing;

/**
 * a: B
 */
public class AldorDeclare extends DeclareNode<AldorDeclPart> {
    public AldorDeclare(AldorDeclPart element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
    }

    public AldorDeclare(List<Syntax> l) {
        super(createMissing(), l);
    }

    @Override
    public Syntax lhs() {
        return child(0);
    }

    @Override
    public Syntax rhs() {
        return child(1);
    }

    @Override
    public String name() {
        return "Decl";
    }
}
