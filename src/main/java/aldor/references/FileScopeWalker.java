package aldor.references;

import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.ScopeFormingElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class FileScopeWalker {
    private static final Logger LOG = Logger.getInstance(FileScopeWalker.class);
    public static final Key<ScopeContext> scopeContextKey = new Key<>("ScopeContext");
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
            while ((thisScope != null) && !(thisScope instanceof ScopeFormingElement)) {
                thisScope = thisScope.getParent();
            }
        }
    }

    @Nullable
    public static PsiElement lookupBySymbolFile(PsiElement element) {
        AnnotationFileManager fileManager = AnnotationFileManager.getAnnotationFileManager(element.getProject());

        AldorIdentifier ident = fileManager.lookupReference(element);

        if (ident == null) {
            return null;
        }
        // It might be worth pondering using the stub tree for lookup at this point.
        // however, it isn't worth it at the moment as the current line number based
        // lookup for symbols means that the originating file is parsed.  There's some
        // "gist" idea in newer intellij that might allow a storing a line number/offset map

        Optional<AldorDefine> outerDefineMaybe = ofNullable(PsiTreeUtil.getContextOfType(ident, AldorDefine.class, true));
        AldorDeclare declare = PsiTreeUtil.getContextOfType(ident, AldorDeclare.class, true, AldorDefine.class);
        //noinspection ObjectEquality
        if (outerDefineMaybe.flatMap(AldorDefine::defineIdentifier).map(id -> id == ident).orElse(false)) {
            return outerDefineMaybe.get();
        }
        if (declare != null) {
            return declare;
        }
        return ident;
     }


}

