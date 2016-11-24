package aldor.syntax.components;

import aldor.psi.AldorDeclPart;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * a: B
 */
public class Declare extends SyntaxNode<AldorDeclPart> {
    public Declare(AldorDeclPart element, @NotNull List<Syntax> arguments) {
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

    @Override
    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitDeclaration(this);
    }
}
