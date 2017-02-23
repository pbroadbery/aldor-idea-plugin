package aldor.psi.stub.impl;

import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.Syntax;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

import java.util.Optional;

public class AldorDeclareConcreteStub extends StubBase<AldorDeclare> implements AldorDeclareStub {
    private final Syntax lhsSyntax;

    public AldorDeclareConcreteStub(StubElement<?> parent, IStubElementType<?, ?> elementType, Syntax lhsSyntax) {
        super(parent, elementType);
        this.lhsSyntax = lhsSyntax;
    }

    @Override
    public Optional<Syntax> declareId() {
        Syntax syntax = lhsSyntax;
        while (syntax.is(Apply.class)) {
            syntax = syntax.as(Apply.class).operator();
        }
        if (syntax.is(Id.class)) {
            return Optional.of(syntax.as(Id.class));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> declareIdName() {
        return declareId().filter(idSyntax -> idSyntax.is(Id.class))
                .map(idSyntax -> idSyntax.as(Id.class))
                .map(Id::symbol);
    }

    @Override
    public Syntax lhsSyntax() {
        return lhsSyntax;
    }

    @Override
    public boolean isDeclareOfId() {
        return lhsSyntax.is(Id.class);
    }
}
