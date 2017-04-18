package aldor.build.module;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.MissingAnnotationFile;
import aldor.symbolfile.PopulatedAnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import aldor.util.AnnotatedOptional;
import aldor.util.sexpr.SExpressions;
import aldor.util.sexpr.SymbolPolicy;
import aldor.util.sexpr.impl.SExpressionReadException;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class AnnotationFileManager implements Disposable {
    private static final Logger LOG = Logger.getInstance(AnnotationFileManager.class);

    private static final Key<AnnotationFileManager> MGR_KEY = new Key<>("AnnotationFileManager");
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // TODO: Need to track these, and use them
    private final Set<String> dirtyFiles;
    @SuppressWarnings("unused")
    private final Set<String> updatingFiles;
    private final Map<String, AnnotationFile> annotationFileForFile;
    private final Map<String, LineNumberMap> lineNumberMapForFile;
    private final AnnotationFileBuilder annotationFileBuilder;
    private final Project project;

    public AnnotationFileManager(Project project) {
        dirtyFiles = new HashSet<>();
        updatingFiles = new HashSet<>();
        annotationFileForFile = Maps.newHashMap();
        lineNumberMapForFile = Maps.newHashMap();
        annotationFileBuilder = new AnnotationFileBuilderImpl();
        this.project = project;
    }

    @NotNull
    public static AnnotationFileManager getAnnotationFileManager(Project project) {
        AnnotationFileManager mgr = project.getUserData(MGR_KEY);
        if (mgr == null) {
            mgr = new AnnotationFileManager(project);
            project.putUserData(MGR_KEY, mgr);
            Disposer.register(project, mgr);
        }
        return mgr;
    }

    @Override
    public void dispose() {
        LOG.info("Disposing of annotation file manager");
    }

    @NotNull
    public AnnotationFile annotationFile(@NotNull PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return new MissingAnnotationFile(null, "no file");
        }
        if (!annotationFileForFile.containsKey(virtualFile.getPath())) {
            annotationFileForFile.put(virtualFile.getPath(), annotatedFile(psiFile));
            LineNumberMap map = new LineNumberMap(psiFile);
            this.lineNumberMapForFile.put(virtualFile.getPath(), map);

        }
        return annotationFileForFile.get(virtualFile.getPath());
    }

    // ToDo: There's probably a fair amount of missing thread-safety here.
    public void invalidate(VirtualFile file) {
        annotationFileForFile.remove(file.getPath());
    }

    @NotNull
    private AnnotationFile annotatedFile(PsiFileSystemItem psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        VirtualFileSystem vfs = virtualFile.getFileSystem();

        AldorModuleManager moduleManager = AldorModuleManager.getInstance(psiFile.getProject());
        String buildFilePath = moduleManager.annotationFileForSourceFile(psiFile);
        if (buildFilePath == null) {
            return new MissingAnnotationFile(virtualFile, "No content directory");
        }
        else {
            VirtualFile buildFile = vfs.findFileByPath(buildFilePath);
            if (buildFile == null) {
                buildFile = vfs.refreshAndFindFileByPath(buildFilePath);
            }

            if (buildFile == null) {
                return new MissingAnnotationFile(virtualFile, "Missing .abn file: "+ buildFilePath);
            }
            try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(buildFile.getInputStream(), StandardCharsets.US_ASCII))) {

                try {
                    return new PopulatedAnnotationFile(virtualFile.getPath(), SExpressions.read(reader, SymbolPolicy.ALLCAPS));
                }
                catch (SExpressionReadException e) {
                    LOG.error("When reading file: " + buildFilePath + ": " + reader.getLineNumber(), e);
                    return new MissingAnnotationFile(virtualFile, e.getMessage());
                }
            } catch (IOException e) {
                LOG.error("When creating annotation file " + buildFilePath, e);
                return new MissingAnnotationFile(virtualFile, e.getMessage());
            }
            catch (RuntimeException e) {
                LOG.error("Failed to parse annotation file " + buildFilePath, e);
                return new MissingAnnotationFile(virtualFile, e.getMessage());
            }
        }
    }

    @Nullable
    public SrcPos findSrcPosForElement(PsiElement element) {
        LineNumberMap map = lineNumberMapForFile(element.getContainingFile());
        if (map == null) {
            return null;
        }
        return map.findSrcPosForElement(element);
    }

    @Nullable
    public AldorIdentifier findElementForSrcPos(PsiFile file, SrcPos srcPos) {
        LineNumberMap map = lineNumberMapForFile(file);
        if (map == null) {
            return null;
        }
        return map.findPsiElementForSrcPos(file, srcPos.lineNumber(), srcPos.columnNumber());
    }

    private LineNumberMap lineNumberMapForFile(PsiFile file) {
        if (file.getVirtualFile() == null) {
            return null;
        }
        annotationFile(file);
        return lineNumberMapForFile.get(file.getVirtualFile().getPath());
    }


    public Future<Void> requestRebuild(PsiFile psiFile) {
        return annotationFileBuilder.invokeAnnotationBuild(psiFile);
    }

    public AnnotatedOptional<Syme,String> symeForElement(PsiElement element) {
        AnnotationFile annotationFile = annotationFile(element.getContainingFile());

        AnnotatedOptional<SrcPos, String> srcPosMaybe = AnnotatedOptional.ofNullable(findSrcPosForElement(element), () -> "No source found");

        return srcPosMaybe.flatMap(srcPos -> AnnotatedOptional.ofNullable(annotationFile.lookupSyme(srcPos), () -> "Failed to find symbol"));
    }

    @Nullable
    public AldorIdentifier lookupReference(@NotNull PsiElement element) {
        SrcPos srcPos = findSrcPosForElement(element);
        if (srcPos == null) {
            return null;
        }
        AnnotationFile annotationFile = annotationFile(element.getContainingFile());
        Syme syme = annotationFile.lookupSyme(srcPos);
        if (syme == null) {
            LOG.info("No Symbol found at " + srcPos);
            return null;
        }

        if (syme.srcpos() != null) {
            PsiFile theFile = psiFileForFileName(element.getContainingFile(), syme.srcpos().fileName() + ".as");
            return (theFile == null) ? null : findElementForSrcPos(theFile, syme.srcpos());
        }

        String refSourceFile = refSourceFile(syme);
        if (refSourceFile == null) {
            return null;
        }
        PsiFile refFile = psiFileForFileName(element.getContainingFile(), refSourceFile);
        if (refFile == null) {
            return null;
        }
        AnnotationFile refAnnotationFile = annotationFile(refFile);
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

    @Nullable
    private String refSourceFile(Syme syme) {
        Syme original = syme.original();
        @Nullable
        String refName;
        if (original == null) {
            refName = syme.archiveLib();
        }
        else {
            if (original.typeCode() == -1) {
                refName = null;
            }
            else {
                refName = original.library();
            }
        }
        if (refName == null) {
            return null;
        }
        return StringUtil.trimExtension(refName) + ".as";
    }

    @Nullable
    private PsiFile psiFileForFileName(PsiElement referer, String sourceFile) {
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

}


