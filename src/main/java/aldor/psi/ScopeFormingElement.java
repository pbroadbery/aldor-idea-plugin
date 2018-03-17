package aldor.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScopeFormingElement extends PsiElement {

    @Override
    boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                @Nullable PsiElement lastParent,
                                @NotNull PsiElement place);
}
