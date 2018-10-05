package aldor.psi.impl;

import aldor.psi.AldorDeclBlock;
import aldor.psi.AldorPsiUtils;
import aldor.psi.AldorPsiUtils.Binding;
import aldor.psi.ScopeFormingElement;
import aldor.references.ScopeContext;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static aldor.references.FileScopeWalker.scopeContextKey;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorDeclBlockMixin extends ASTWrapperPsiElement implements AldorDeclBlock, ScopeFormingElement {
    private static final Logger LOG = Logger.getInstance(AldorDeclBlockMixin.class);

    protected AldorDeclBlockMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        List<Binding> bindings = AldorPsiUtils.childBindings(this);

        for (Binding binding: bindings) {
            if (PsiTreeUtil.isAncestor(lastParent, binding.element(), false)) {
                continue;
            }
            boolean ret = processor.execute(binding.element(), state.put(scopeContextKey, ScopeContext.DeclBlock));
            if (!ret) {
                return false;
            }
        }
        return true;

    }
}
