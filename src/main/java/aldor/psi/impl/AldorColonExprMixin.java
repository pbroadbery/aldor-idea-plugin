package aldor.psi.impl;

import aldor.psi.AldorColonExpr;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorColonExprMixin extends ASTWrapperPsiElement implements AldorColonExpr {

    protected AldorColonExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }
        PsiElement lhs = this.getFirstChild();
        //noinspection ObjectEquality
        if (lastParent != lhs) {
            Syntax lhsSyntax = SyntaxPsiParser.parse(lhs);
            if (lhsSyntax != null) {
                if (!lhsSyntax.psiElement().processDeclarations(processor, state, this, place)) {
                    return false;
                }
            }
        }
        return true;
    }
}
