package aldor.syntax.components;

import aldor.psi.AldorColonExpr;
import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * a: B
 */
public class SpadDeclare extends DeclareNode<AldorColonExpr> {
    public SpadDeclare(AldorColonExpr element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
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
        return "SDecl";
    }

}
