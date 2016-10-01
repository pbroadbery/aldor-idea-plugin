package aldor.psi.impl;

import aldor.psi.AldorDeclPart;
import aldor.psi.AldorPsiUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorDeclMixin extends ASTWrapperPsiElement implements AldorDeclPart {

    protected AldorDeclMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }
        PsiElement lhs = this.getFirstChild();
        if (lastParent != lhs) {
            AldorPsiUtils.Syntax lhsSyntax = AldorPsiUtils.parse(lhs);
            if (lhsSyntax != null) {
                lhsSyntax.psiElement().processDeclarations(processor, state, this, place);
            }
        }
        return true;
    }
}
