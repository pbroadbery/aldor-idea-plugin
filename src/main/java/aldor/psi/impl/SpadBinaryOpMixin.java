package aldor.psi.impl;

import aldor.psi.AldorExpr;
import aldor.psi.SpadBinaryOp;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

import java.util.List;

public class SpadBinaryOpMixin extends ASTWrapperPsiElement implements AldorExpr, SpadBinaryOp {
    public SpadBinaryOpMixin(ASTNode node) {
        super(node);
    }


    @Override
    public PsiElement getOp() {
        return getChildren()[1];
    }

    @Override
    public List<AldorExpr> getExprList() {
        throw new UnsupportedOperationException("oops");
    }
}