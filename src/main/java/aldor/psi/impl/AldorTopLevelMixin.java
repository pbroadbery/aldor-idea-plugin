package aldor.psi.impl;

import aldor.psi.AldorPsiUtils;
import aldor.psi.AldorTopLevel;
import aldor.psi.AldorWhereRhs;
import aldor.psi.ScopeFormingElement;
import aldor.references.FileScopeWalker;
import aldor.references.ScopeContext;
import aldor.util.StringUtilsAldorRt;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static aldor.references.FileScopeWalker.definitionTypeKey;
import static aldor.references.FileScopeWalker.scopeContextKey;

public abstract class AldorTopLevelMixin extends ASTWrapperPsiElement implements AldorTopLevel, ScopeFormingElement {
    private static final Logger LOG = Logger.getInstance(AldorTopLevelMixin.class);

    public AldorTopLevelMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        List<AldorPsiUtils.Binding> bindings = AldorPsiUtils.childBindings(this);

        for (AldorPsiUtils.Binding binding: bindings) {
            if (PsiTreeUtil.isAncestor(lastParent, binding.element(), false)) {
                continue;
            }
            ResolveState newState = state
                    .put(scopeContextKey, ScopeContext.DeclBlock)
                    .put(definitionTypeKey, binding.definitionType());
            boolean ret = processor.execute(binding.element(), newState);
            if (!ret) {
                return false;
            }
        }
        return true;
    }
}
