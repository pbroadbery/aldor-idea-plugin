package aldor.syntax.components;

import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import com.intellij.psi.PsiElement;

import java.util.List;

public abstract class DeclareNode<T extends PsiElement> extends SyntaxNode<T> {

    protected DeclareNode(T element, List<Syntax> arguments) {
        super(element, arguments);
    }

    protected DeclareNode(SyntaxRepresentation<T> representation, List<Syntax> arguments) {
        super(representation, arguments);
    }

    public Syntax lhs() {
        return child(0);
    }

    public Syntax rhs() {
        return child(1);
    }

    @Override
    public <R> R accept(SyntaxVisitor<R> syntaxVisitor) {
        return syntaxVisitor.visitDeclaration(this);
    }

}
