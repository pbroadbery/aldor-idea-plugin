package aldor.psi.impl;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorVisitor;
import aldor.psi.stub.AldorDeclareStub;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof AldorVisitor) {
            accept((AldorVisitor)visitor);
        } else {
            super.accept(visitor);
        }
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public void accept(@NotNull AldorVisitor visitor) {
        visitor.visitDeclare(this);
    }
}
