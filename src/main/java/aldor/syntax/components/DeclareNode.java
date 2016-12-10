package aldor.syntax.components;

import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import com.intellij.psi.PsiElement;

import java.util.List;

public abstract class DeclareNode<T extends PsiElement> extends SyntaxNode<T> {

    protected DeclareNode(T element, List<Syntax> arguments) {
        super(element, arguments);
    }

    public abstract Syntax lhs();

    public abstract Syntax rhs();

    @Override
    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitDeclaration(this);
    }

}
