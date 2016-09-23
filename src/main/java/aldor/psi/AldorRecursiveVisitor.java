package aldor.psi;

import com.intellij.psi.PsiElement;

public class AldorRecursiveVisitor extends AldorVisitor {
    @Override
    public void visitElement(PsiElement o) {
        o.acceptChildren(this);
    }
}
