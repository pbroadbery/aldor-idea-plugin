package aldor.psi.impl;

import aldor.psi.AldorComma;
import aldor.psi.AldorCommaItem;
import aldor.psi.ScopeFormingElement;
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
public abstract class AldorCommaMixin extends ASTWrapperPsiElement implements AldorComma, ScopeFormingElement {

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

        // Not sure if diving through syntax to get to definitions is the right approach
        for (AldorCommaItem commaItem: getCommaItemList()) {
            if (PsiTreeUtil.isAncestor(lastParent, commaItem, false)) {
                continue;
            }
            Syntax syntax = SyntaxPsiParser.parse(commaItem);
            if (syntax == null) {
                continue;
            }

            if (PsiTreeUtil.isAncestor(lastParent, syntax.psiElement(), false)) {
                continue;
            }

            if (!syntax.psiElement().processDeclarations(processor, state, null, place)) {
                return false;
            }
        }
        return true;
    }
}
