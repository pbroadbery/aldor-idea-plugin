package aldor.syntax;

import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Id;
import aldor.syntax.components.Other;
import aldor.syntax.components.SyntaxNode;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class SyntaxVisitor<T> {

    @Nullable
    public T visitSyntax(Syntax syntax) {
        throw new UnsupportedOperationException("visitSyntax: " + this.getClass().getName() + " " + syntax.getClass().getName());
    }

    public T visitSyntaxNode(SyntaxNode<?> node) {return visitSyntax(node);}

    public T visitApply(Apply apply) {return visitSyntaxNode(apply);}

    public T visitComma(Comma comma) {return visitSyntaxNode(comma);}
    public T visitId(Id id) {return visitSyntax(id);}

    public T visitOther(Other other) {return visitSyntax(other);}

}
