package aldor.psi.impl;

import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorDeclareImpl extends StubBasedPsiElementBase<AldorDeclareStub> implements AldorDeclare {

    protected AldorDeclareImpl(AldorDeclareStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    protected AldorDeclareImpl(AldorDeclareStub stub, IStubElementType<AldorDeclareStub, AldorDeclare> elementType) {
        super(stub, elementType);
    }

    protected AldorDeclareImpl(ASTNode node) {
        super(node);
    }

}
