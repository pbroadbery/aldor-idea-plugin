package aldor.references;

import aldor.annotations.AnnotationFileNavigator;
import aldor.annotations.AnnotationFileNavigatorManager;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorTopLevel;
import aldor.psi.ScopeFormingElement;
import aldor.psi.index.AldorDefineTopLevelIndex;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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
        AnnotationFileNavigator fileNavigator = AnnotationFileNavigatorManager.instance.getInstance(element.getProject());

        AldorIdentifier ident = fileNavigator.lookupReference(element);

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

    @Nullable
    public static PsiElement lookupByIndex(@SuppressWarnings("TypeMayBeWeakened") AldorIdentifier element) {
        Collection<AldorDefine> elements = AldorDefineTopLevelIndex.instance.get(element.getText(), element.getProject(), GlobalSearchScope.allScope(element.getProject()));
        LOG.info("Found elements " + elements.size());
        if (elements.size() != 1) {
            return null;
        }
        return elements.iterator().next().defineIdentifier().orElse(null);
    }
}

