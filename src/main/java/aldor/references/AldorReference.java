package aldor.references;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface AldorReference extends PsiReference, EmptyResolveMessageProvider {
    PsiElement resolveMacro();
}
