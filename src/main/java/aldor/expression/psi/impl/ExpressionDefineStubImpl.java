package aldor.expression.psi.impl;

import aldor.expression.ExpressionDefineStub;
import aldor.expression.psi.ExpressionDefine;
import aldor.expression.psi.ExpressionVisitor;
import aldor.psi.AldorVisitor;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

public class ExpressionDefineStubImpl extends StubBasedPsiElementBase<ExpressionDefineStub> implements ExpressionDefine {

    public ExpressionDefineStubImpl(@NotNull ExpressionDefineStub stub,
                                    @NotNull IStubElementType<ExpressionDefineStub, ExpressionDefine> nodeType) {
        super(stub, nodeType);
    }

    @Override
    public PsiElement getParent() {
        return getParentByStub();
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public void accept(@NotNull ExpressionVisitor visitor) {
        visitor.visitDefine(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof AldorVisitor) {
            accept((AldorVisitor)visitor);
        } else {
            super.accept(visitor);
        }
    }
}
