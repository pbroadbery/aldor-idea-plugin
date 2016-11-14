package aldor.expression.psi.impl;

import aldor.expression.ExpressionDefineStub;
import aldor.expression.psi.ExpressionDefine;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.PsiElement;
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
}
