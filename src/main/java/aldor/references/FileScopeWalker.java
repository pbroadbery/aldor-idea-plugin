package aldor.references;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
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

    public static PsiElement lookupBySymbolFile(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();
        if (containingFile.getVirtualFile() == null) {
            return null;
        }
        Optional<Module> moduleAndRoot = AldorModuleManager.getInstance(project).aldorModuleForFile(containingFile.getVirtualFile());
        if (!moduleAndRoot.isPresent()) {
            return null;
        }
        Module module = moduleAndRoot.get();
        AnnotationFileManager fileManager = AnnotationFileManager.getAnnotationFileManager(module);
        if (fileManager == null) {
            return null;
        }
        AnnotationFile annotationFile = fileManager.annotationFile(containingFile);
        SrcPos srcPos = fileManager.findSrcPosForElement(element);
        Syme syme = annotationFile.lookupSyme(srcPos);
        if (syme == null) {
            LOG.info("No Symbol found at " + srcPos);
            return null;
        }

        if (syme.srcpos() != null) {
            PsiFile theFile = psiFileForFileName(module, syme.srcpos().fileName() + ".as");
            return fileManager.findElementForSrcPos(theFile, syme.srcpos());
        }

        Syme original = syme.original();
        String refName;
        if (original == null) {
            refName = syme.archiveLib();
        }
        else {
            if (original.typeCode() == -1) {
                LOG.info("No typecode for " + srcPos + " " + syme);
                return null;
            }

            refName = original.library();
        }
        if (refName == null) {
            LOG.info("No library for " + srcPos + " " + syme + " " + original);
            return null;
        }
        String refSourceFile = StringUtil.trimExtension(refName) + ".as";
        PsiFile refFile = psiFileForFileName(module, refSourceFile);

        AnnotationFile refAnnotationFile = fileManager.annotationFile(refFile);
        Syme refSyme = refAnnotationFile.symeForNameAndCode(syme.name(), syme.typeCode());
        if (refSyme == null) {
            return null;

        }
        if (refSyme.srcpos() == null) {
            LOG.info("No source pos for " + refSourceFile + " " + element.getText());
            return null;
        }
        LOG.info("Found reference to " + element.getText() + " at " + refSyme.srcpos());
        return fileManager.findElementForSrcPos(refFile, refSyme.srcpos());
    }

    @Nullable
    private static PsiFile psiFileForFileName(Module module, String sourceFile) {
        PsiFile[] refFiles = FilenameIndex.getFilesByName(module.getProject(), sourceFile, GlobalSearchScope.moduleScope(module));
        PsiFile refFile;
        if (refFiles.length > 1) {
            LOG.info("Multiple files called " + sourceFile);
            refFile = null; // ?? Multi???
        }
        else if (refFiles.length == 0) {
            LOG.info("No file " + sourceFile);
            refFile = null;
        }
        else {
            refFile = refFiles[0];
        }
        return refFile;
    }
}

