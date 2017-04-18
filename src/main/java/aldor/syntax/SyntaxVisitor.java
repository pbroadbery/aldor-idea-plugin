package aldor.syntax;

import aldor.syntax.components.AbstractId;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.EnumList;
import aldor.syntax.components.Id;
import aldor.syntax.components.InfixedId;
import aldor.syntax.components.Other;
import aldor.syntax.components.SyntaxNode;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class SyntaxVisitor<T> {

    @Nullable
    public T visitSyntax(Syntax syntax) {
        throw new UnsupportedOperationException("visitSyntax: " + this.getClass().getName() + " " + syntax.getClass().getName());
    }

    public T visitSyntaxNode(SyntaxNode<?> node) {
        return visitSyntax(node);
    }

    public T visitDeclaration(DeclareNode<?> node) {
        return visitSyntaxNode(node);
    }

    @Nullable
    public T visitApply(Apply apply) {
        return visitSyntaxNode(apply);
    }

    public T visitComma(Comma comma) {
        return visitSyntaxNode(comma);
    }

    @Nullable
    public T visitAnyId(AbstractId id) {
        return visitSyntax(id);
    }

    @Nullable
    public T visitId(Id id) {
        return visitAnyId(id);
    }


    @Nullable
    public T visitInfixedId(InfixedId id) {
        return visitAnyId(id);
    }

    public T visitOther(Other other) {
        return visitSyntax(other);
    }

    public T visitEnumList(EnumList enumList) {
        return visitSyntax(enumList);
    }
}
