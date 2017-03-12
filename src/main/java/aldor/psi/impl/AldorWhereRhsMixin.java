package aldor.psi.impl;

import aldor.psi.AldorDefine;
import aldor.psi.AldorLambda;
import aldor.psi.AldorWhereBlock;
import aldor.psi.AldorWhereRhs;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class AldorWhereRhsMixin extends ASTWrapperPsiElement implements AldorWhereRhs {

    public AldorWhereRhsMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        WhereRhsVisitor visitor = new WhereRhsVisitor(elt -> elt.processDeclarations(processor, state, null, place), lastParent);
        this.accept(visitor);
        return (visitor.returnValue() == null);
    }

    private class WhereRhsVisitor extends ReturningAldorVisitor<Boolean> {
        private final Function<PsiElement, Boolean> processor;
        private final PsiElement lastElement;

        WhereRhsVisitor(Function<PsiElement, Boolean> processor, PsiElement lastElement) {
            this.processor = processor;
            this.lastElement = lastElement;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (PsiTreeUtil.isAncestor(lastElement, element, false)) {
                return;
            }
            doAcceptChildren(element);
        }

        @Override
        public void visitDefine(@NotNull AldorDefine define) {
            Boolean ret = processor.apply(define);
            if (!ret) {
                returnValue(false);
            }
        }

        @Override
        public void visitLambda(@NotNull AldorLambda o) {
        }

        @Override
        public void visitWhereBlock(@NotNull AldorWhereBlock o) {
            o.getFirstChild().accept(this);
        }
    }

}
