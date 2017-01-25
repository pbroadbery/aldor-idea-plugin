package aldor.psi.impl;

import aldor.psi.AldorExpr;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorSpadExprMixin extends ASTWrapperPsiElement implements AldorExpr {

    protected AldorSpadExprMixin(@NotNull ASTNode node) {
        super(node);
    }

}
