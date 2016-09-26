package aldor.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

public class FileScopeWalker {

    public void resolveAndWalk(PsiScopeProcessor scopeProcessor, PsiElement initial) {
        PsiElement thisScope = initial.getParent();
        PsiElement lastScope = initial;
        ResolveState state = ResolveState.initial();

        while (thisScope != null) {
            if (!thisScope.processDeclarations(scopeProcessor, state, lastScope, initial)) {
                break;
            }

            lastScope = thisScope;
            thisScope = thisScope.getParent();
        }
    }

}

