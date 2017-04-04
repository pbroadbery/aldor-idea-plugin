package aldor.psi.stub.impl;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorPsiUtils;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.DeclareFunctions;
import aldor.syntax.Syntax;
import aldor.syntax.components.AbstractId;
import aldor.syntax.components.DeclareNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

import java.util.Objects;
import java.util.Optional;

public class AldorDeclareConcreteStub extends StubBase<AldorDeclare> implements AldorDeclareStub {
    private final Syntax syntax;
    private final boolean isCategoryDeclaration;

    public AldorDeclareConcreteStub(StubElement<?> parent, IStubElementType<AldorDeclareStub, AldorDeclare> elementType,
                                    Syntax syntax, AldorPsiUtils.IContainingBlockType blockType) {
        super(parent, elementType);
        this.syntax = syntax;
        this.isCategoryDeclaration = Objects.equals(blockType, AldorPsiUtils.WITH);
    }

    public Optional<AbstractId> declareId() {
        if (syntax.is(DeclareNode.class)) {
            return DeclareFunctions.declareId(syntax.as(DeclareNode.class).lhs());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> declareIdName() {
        return declareId().map(AbstractId::symbol);
    }

    @Override
    public boolean isDeclareOfId() {
        if (!syntax.is(DeclareNode.class)) {
            return false;
        }
        return syntax.as(DeclareNode.class).lhs().is(AbstractId.class);
    }

    @Override
    public boolean isCategoryDeclaration() {
        return isCategoryDeclaration;
    }

    @Override
    public Syntax declareType() {
        return syntax.as(DeclareNode.class).rhs();
    }

    @Override
    @Deprecated
    public Syntax rhsSyntax() {
        return syntax.as(DeclareNode.class).rhs();
    }

    @Override
    public Syntax syntax() {
        return syntax;
    }

}
