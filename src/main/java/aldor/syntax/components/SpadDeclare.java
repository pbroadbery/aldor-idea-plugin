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

    public SpadDeclare(List<Syntax> children) {
        super(SyntaxRepresentation.createMissing(), children);
    }

    @Override
    public String name() {
        return "SDecl";
    }

}
