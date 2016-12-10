package aldor.psi.impl;

import aldor.psi.AldorComma;
import aldor.psi.AldorCommaItem;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorCommaMixin extends ASTWrapperPsiElement implements AldorComma {

    protected AldorCommaMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        //noinspection ObjectEquality
        if (lastParent == this) {
            return true;
        }

        if (this.getCommaItemList().size() <= 1) {
            return true;
        }
        for (AldorCommaItem commaItem: getCommaItemList()) {
            Syntax syntax = SyntaxPsiParser.parse(commaItem);
            if (syntax == null) {
                continue;
            }

            if (PsiTreeUtil.isAncestor(lastParent, syntax.psiElement(), false)) {
                continue;
            }

            if (!syntax.psiElement().processDeclarations(processor, state, this, place)) {
                return false;
            }
        }
        return true;
    }
}
