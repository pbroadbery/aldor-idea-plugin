package aldor.build.module;

import aldor.lexer.IndentWidthCalculator;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.MissingAnnotationFile;
import aldor.symbolfile.PopulatedAnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SymbolPolicy;
import aldor.util.sexpr.impl.SExpressionReadException;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharSequenceSubSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    private final IndentWidthCalculator widthCalculator;

    public AnnotationFileManager() {
        dirtyFiles = new HashSet<>();
        updatingFiles = new HashSet<>();
        annotationFileForFile = Maps.newHashMap();
        lineNumberMapForFile = Maps.newHashMap();
        annotationFileBuilder = new AnnotationFileBuilder.AnnotationFileBuilderImpl();
        widthCalculator = new IndentWidthCalculator();
    }

    @Nullable
    public static AnnotationFileManager getAnnotationFileManager(@NotNull Module module) {
        ModuleType<?> type = ModuleType.get(module);
        if (!Objects.equals(type, AldorModuleType.instance())) {
            return null;
        }
        AnnotationFileManager annotationManager = module.getUserData(MGR_KEY);
        if (annotationManager == null) {
            annotationManager = new AnnotationFileManager();
            AnnotationFileManager finalAnnotationManager = annotationManager;
            Disposer.register(module, annotationManager);
            module.putUserData(MGR_KEY, annotationManager);
            annotationManager = finalAnnotationManager;
        }

        return annotationManager;
    }

    @Override
    public void dispose() {
        LOG.info("Disposing of annotation file manager");
    }

    @NotNull
    public AnnotationFile annotationFile(PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
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
        String buildFilePath = moduleManager.annotationFileForSourceFile(virtualFile);
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
                    return new PopulatedAnnotationFile(virtualFile.getPath(), SExpression.read(reader, SymbolPolicy.ALLCAPS));
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
        LineNumberMap map = lineNumberMapForFile.get(element.getContainingFile().getVirtualFile().getPath());
        if (map == null) {
            return null;
        }
        return map.findSrcPosForElement(element);
    }

    @Nullable
    public PsiElement findElementForSrcPos(PsiFile file, SrcPos srcPos) {
        LineNumberMap map = lineNumberMapForFile.get(file.getVirtualFile().getPath());
        if (map == null) {
            return null;
        }
        return map.findPsiElementForSrcPos(file, srcPos.lineNumber(), srcPos.columnNumber());
    }

    public Future<Void> requestRebuild(PsiFile psiFile) {
        return annotationFileBuilder.invokeAnnotationBuild(psiFile);
    }

    private class LineNumberMap {
        private final NavigableMap<Integer, Integer> lineNumberForOffset;
        private final Map<Integer, Integer> offsetForLineNumber;

        LineNumberMap(@SuppressWarnings("TypeMayBeWeakened") PsiFile file) {
            this.lineNumberForOffset = scanLines(file);
            offsetForLineNumber = lineNumberForOffset.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        }

        public int offsetForLine(int lineNumber) {
            return offsetForLineNumber.get(lineNumber);
        }

        private NavigableMap<Integer, Integer> scanLines(PsiElement file) {
            NavigableMap<Integer, Integer> lineForOffset = new TreeMap<>();
            String text = file.getText();
            lineForOffset.put(0, 0);
            int line = 1;
            int len = text.length();
            for (int i=0; i<len; i++) {
                if (text.charAt(i) == '\n') {
                    // +1 is because we want the offset to be at the start of the line, not the newline char itself.
                    lineForOffset.put(i+1, line);
                    line++;
                }
            }
            return lineForOffset;
        }

        public SrcPos findSrcPosForElement(PsiElement element) {
            int textOffset = element.getTextOffset();
            Integer lineOffset = lineNumberForOffset.headMap(textOffset, true).lastKey();
            int column = widthCalculator.width(element.getContainingFile().getText().subSequence(lineOffset, textOffset));
            return new SrcPos(StringUtil.trimExtension(element.getContainingFile().getName()), 1 + lineNumberForOffset.get(lineOffset), 1+column);
        }

        @Nullable
        public PsiElement findPsiElementForSrcPos(PsiFile file, int line, int col) {
            int lineOffset = offsetForLine(line-1);
            int colOffset = lineOffset + widthCalculator.offsetForWidth(new CharSequenceSubSequence(file.getText(), lineOffset, file.getTextLength()), col);
            return PsiTreeUtil.findElementOfClassAtOffset(file, colOffset, AldorIdentifier.class, true);
        }
    }

}
