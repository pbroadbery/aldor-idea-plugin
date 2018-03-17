package aldor.psi.impl;

import aldor.psi.AldorDeclaration;
import aldor.psi.ScopeFormingElement;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * local, free, default, etc
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorDeclarationMixin extends ASTWrapperPsiElement implements AldorDeclaration, ScopeFormingElement {

    protected AldorDeclarationMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        //noinspection ObjectEquality
        if (Arrays.stream(this.getChildren()).anyMatch(x -> x == lastParent)) {
            return true;
        }
        if (!processor.execute(this, state)) {
            return false;
        }

        if (getSig() == null) {
            return true;
        }
        Syntax syntax = SyntaxPsiParser.parse(getSig());
        if (syntax != null) {
            Iterable<Syntax> scopes = SyntaxUtils.childScopesForDefineLhs(syntax);
            for (Syntax childScope : scopes) {
                if (!childScope.psiElement().processDeclarations(processor, state, this, place)) {
                    return false;
                }
            }
        }
        return true;
    }

}
