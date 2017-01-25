package aldor.psi;

import com.intellij.psi.PsiElement;

import java.util.List;

public interface SpadBinaryOp extends PsiElement {

    PsiElement getOp();
    List<AldorExpr> getExprList();
}
