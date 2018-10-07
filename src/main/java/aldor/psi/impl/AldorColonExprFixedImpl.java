package aldor.psi.impl;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorExpr;
import aldor.psi.AldorVisitor;
import aldor.psi.stub.AldorDeclareStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static aldor.psi.elements.AldorTypes.KW_COLON;

/*
 * This is required because the default Grammar code generation can't correctly figure out what
 * constructors are needed; so added them and used here.
 */
public class AldorColonExprFixedImpl extends AldorColonExprMixin {

    public AldorColonExprFixedImpl(AldorDeclareStub stub, IStubElementType<AldorDeclareStub, AldorDeclare> type) {
        super(stub, type);
    }

    public AldorColonExprFixedImpl(ASTNode node) {
        super(node);
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public void accept(@NotNull AldorVisitor visitor) {
        visitor.visitColonExpr(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof AldorVisitor) {
            accept((AldorVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    @Override
    @NotNull
    public List<AldorExpr> getExprList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, AldorExpr.class);
    }

    @Override
    @NotNull
    public PsiElement getKWColon() {
        return notNullChild(findChildByType(KW_COLON));
    }

}
