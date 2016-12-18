package aldor.references;

import aldor.build.module.AnnotationFileManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class FileScopeWalker {
    private static final Logger LOG = Logger.getInstance(FileScopeWalker.class);

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

    @Nullable
    public static PsiElement lookupBySymbolFile(PsiElement element) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(element.getProject());
        Optional<PsiFile> fileMaybe = Optional.ofNullable(element.getContainingFile());
        Optional<VirtualFile> vfMaybe = fileMaybe.flatMap(psiFile -> Optional.ofNullable(psiFile.getVirtualFile()));
        Optional<Module> moduleMaybe = vfMaybe.flatMap(vf -> {
            return Optional.ofNullable(rootManager.getFileIndex().getModuleForFile(vf));
        });
        Optional<AnnotationFileManager> fileManagerMaybe = moduleMaybe.flatMap(AnnotationFileManager::getAnnotationFileManager);
        if (!fileManagerMaybe.isPresent()) {
            return null;
        }
        AnnotationFileManager fileManager = fileManagerMaybe.get();
        return fileManager.lookupReference(element);
    }


}

