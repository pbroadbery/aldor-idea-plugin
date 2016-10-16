package aldor.build.module;

import aldor.lexer.IndentWidthCalculator;
import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.PopulatedAnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.util.SExpression;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class AnnotationFileManager implements Disposable {
    private static final Logger LOG = Logger.getInstance(AnnotationFileManager.class);

    private static final Key<AnnotationFileManager> MGR_KEY = new Key<>("AnnotationFileManager");
    private final Set<String> dirtyFiles;
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
        annotationFileBuilder = new AnnotationFileBuilder();
        widthCalculator = new IndentWidthCalculator();
    }

    public void beforeDocumentSaving(PsiFileSystemItem psiFile) {
        LOG.info("About to save: " + psiFile.getName());
        annotationFileBuilder.invokeRebuild(psiFile.getVirtualFile());
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
            annotationFileForFile.put(virtualFile.getPath(), createAnnotationFile(psiFile));
            LineNumberMap map = new LineNumberMap(psiFile);
            this.lineNumberMapForFile.put(virtualFile.getPath(), map);

        }
        return annotationFileForFile.get(virtualFile.getPath());
    }

    @NotNull
    private AnnotationFile createAnnotationFile(PsiFileSystemItem psiFile) {
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
                return new MissingAnnotationFile(virtualFile, "Missing .abn file: "+ buildFilePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildFile.getInputStream(), StandardCharsets.US_ASCII))) {
                return new PopulatedAnnotationFile(virtualFile.getPath(), SExpression.read(reader));
            } catch (IOException e) {
                LOG.error("When creating annotation file " + buildFilePath, e);
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

    private class LineNumberMap {

        private final NavigableMap<Integer, Integer> lineNumberForOffset;

        LineNumberMap(@SuppressWarnings("TypeMayBeWeakened") PsiFile file) {
            this.lineNumberForOffset = scanLines(file);
        }

        public int offsetForLine(int lineNumber) {
            return lineNumberForOffset.get(lineNumber);
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
    }

}
