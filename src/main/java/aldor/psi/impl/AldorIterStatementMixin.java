package aldor.psi.impl;

import aldor.psi.AldorIterRepeatStatement;
import aldor.psi.AldorIterator;
import aldor.psi.AldorIterators;
import aldor.syntax.Syntax;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorIterStatementMixin extends ASTWrapperPsiElement implements AldorIterRepeatStatement {
    private static final Logger LOG = Logger.getInstance(AldorIterStatementMixin.class);
    private static final Key<Syntax> cachedLhsSyntax = new Key<>("LhsSyntax");

    protected AldorIterStatementMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {

        AldorIterators iters = this.getIterators();

        for (AldorIterator iter: iters.getIteratorList()) {
            if (lastParent == iter) {
                continue;
            }
            if (!iter.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }
}
