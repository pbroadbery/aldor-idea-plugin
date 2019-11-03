package aldor.annotations;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import aldor.util.AnnotatedOptional;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class DefaultAnnotationFileNavigator implements AnnotationFileNavigator {
    private static final Logger LOG = Logger.getInstance(DefaultAnnotationFileNavigator.class);
    private static final Key<AnnotationFileNavigator> NAV_KEY = new Key<>(AnnotationFileNavigator.class.getSimpleName());
    private final AnnotationFileManager manager;

    public DefaultAnnotationFileNavigator(AnnotationFileManager annotationFileManager) {
        this.manager = annotationFileManager;
    }

    private Syme lookupAndSelectSyme(@NotNull PsiElement element, SrcPos srcPos, AnnotationFile annotationFile) {
        Collection<Syme> symes = annotationFile.lookupSyme(srcPos);
        return symes.stream().filter(s -> s.name().equals(element.getText())).findFirst().orElse(null);
    }

    @Override
    @Nullable
    public AldorIdentifier lookupReference(@NotNull PsiElement element) {
        SrcPos srcPos = findSrcPosForElement(element);
        if (srcPos == null) {
            return null;
        }
        AnnotationFile annotationFile = manager.annotationFile(element.getContainingFile());
        Syme syme = lookupAndSelectSyme(element, srcPos, annotationFile);
        if (syme == null) {
            LOG.info("No Symbol found at " + srcPos);
            return null;
        }

        if (syme.srcpos() != null) {
            PsiFile theFile = psiFileForFileName(element.getProject(), element.getContainingFile(), syme.srcpos().fileName() + ".as");
            return (theFile == null) ? null : findElementForSrcPos(theFile, syme.srcpos());
        }

        String refSourceFile = manager.refSourceFile(syme);
        if (refSourceFile == null) {
            return null;
        }
        PsiFile refFile = psiFileForFileName(element.getProject(), element.getContainingFile(), refSourceFile);
        if (refFile == null) {
            return null;
        }
        AnnotationFile refAnnotationFile = manager.annotationFile(refFile);
        Syme refSyme = refAnnotationFile.symeForNameAndCode(syme.name(), syme.typeCode());
        if (refSyme == null) {
            return null;

        }
        if (refSyme.srcpos() == null) {
            LOG.info("No source pos for " + refSourceFile + " " + element.getText());
            return null;
        }
        LOG.info("Found reference to " + element.getText() + " at " + refSyme.srcpos());
        return findElementForSrcPos(refFile, refSyme.srcpos());
    }

    @Override
    @Nullable
    public SrcPos findSrcPosForElement(PsiElement element) {
        LineNumberMap map = manager.lineNumberMapForFile(element.getContainingFile());
        if (map == null) {
            return null;
        }
        return map.findSrcPosForElement(element);
    }

    @Nullable
    private PsiFile psiFileForFileName(Project project, PsiElement referer, String sourceFile) {
        PsiFile[] refFiles = FilenameIndex.getFilesByName(project, sourceFile, referer.getResolveScope());
        @Nullable PsiFile refFile;
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

    @Override
    @Nullable
    public AldorIdentifier findElementForSrcPos(PsiFile file, SrcPos srcPos) {
        LineNumberMap map = manager.lineNumberMapForFile(file);
        if (map == null) {
            return null;
        }
        return map.findPsiElementForSrcPos(file, srcPos.lineNumber(), srcPos.columnNumber());
    }

    @Override
    public AnnotatedOptional<Syme,String> symeForElement(PsiElement element) {
        AnnotationFile annotationFile = manager.annotationFile(element.getContainingFile());

        AnnotatedOptional<SrcPos, String> srcPosMaybe = AnnotatedOptional.ofNullable(findSrcPosForElement(element), () -> "No source found");

        return srcPosMaybe.flatMap(srcPos -> {
            Optional<Syme> symeMaybe = annotationFile.lookupSyme(srcPos).stream().filter(s -> s.name().equals(element.getText())).findFirst();

            return AnnotatedOptional.fromOptional(symeMaybe, () -> "Failed to find symbol " + element.getText() + " at " + srcPos);
        });
    }

    public static AnnotationFileNavigator factory(Project project) {
        AnnotationFileNavigator nav = project.getUserData(NAV_KEY);
        if (nav == null) {
            nav = new DefaultAnnotationFileNavigator(AnnotationFileManager.getAnnotationFileManager(project));
            project.putUserData(NAV_KEY, nav);
        }
        return nav;
    }
}
