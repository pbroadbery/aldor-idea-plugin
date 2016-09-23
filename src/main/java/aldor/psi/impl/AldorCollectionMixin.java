package aldor.psi.impl;

import aldor.psi.AldorAnyCollection;
import aldor.psi.AldorIterator;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorCollectionMixin extends ASTWrapperPsiElement implements AldorAnyCollection {

    protected AldorCollectionMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        for (AldorIterator iter: this.getIteratorList()) {
            if (lastParent == iter) {
                continue;
            }
            if (!processor.execute(iter, state)) {
                return false;
            }
            if (!iter.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }


}
