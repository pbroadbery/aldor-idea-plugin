package aldor.syntax.components;

import aldor.psi.AldorIfStatementAnyStatement;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * (1,2,3)
 */
public class If extends SyntaxNode<AldorIfStatementAnyStatement> {

    public If(AldorIfStatementAnyStatement element, @NotNull List<Syntax> arguments) {
        super(element, arguments);
    }

    public If(List<Syntax> l) {
        super(SyntaxRepresentation.createMissing(), l);
    }

    public If(Syntax cond, Syntax thenPart, Syntax elsePart) {
        super(SyntaxRepresentation.createMissing(), Arrays.asList(cond, thenPart, elsePart));
    }

    @Override
    public String name() {
        return "If";
    }

    @Override
    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitIf(this);
    }
}
