package aldor.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

public final class FileScopeWalker {

    // idea is that once we get to file level
    // 1) Look for up to date abn file
    //      - if present, try to dig out details
    //      - if not, resolve to "err, dunno, give me a minute" and trigger a build

    public static void resolveAndWalk(PsiScopeProcessor scopeProcessor, PsiElement initial) {
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

