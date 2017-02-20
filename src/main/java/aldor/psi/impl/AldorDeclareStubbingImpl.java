package aldor.psi.impl;

import aldor.psi.AldorDeclareStubbing;
import aldor.syntax.Syntax;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;

import java.util.Optional;

public final class AldorDeclareStubbingImpl {

    @SuppressWarnings("AbstractClassExtendsConcreteClass")
    public abstract static class AldorDeclareImpl extends StubBasedPsiElementBase<AldorDeclareStubbing.AldorDeclareStub> implements AldorDeclareStubbing.AldorDeclare {

        protected AldorDeclareImpl(AldorDeclareStubbing.AldorDeclareStub stub, IElementType nodeType, ASTNode node) {
            super(stub, nodeType, node);
        }

        protected AldorDeclareImpl(AldorDeclareStubbing.AldorDeclareStub stub, IStubElementType<AldorDeclareStubbing.AldorDeclareStub, AldorDeclareStubbing.AldorDeclare> elementType) {
            super(stub, elementType);
        }

        protected AldorDeclareImpl(ASTNode node) {
            super(node);
        }

    }

    public static class AldorDeclareConcreteStub extends StubBase<AldorDeclareStubbing.AldorDeclare> implements AldorDeclareStubbing.AldorDeclareStub {
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

}
