package aldor.annotator;

import aldor.builder.files.BuildFiles;
import aldor.lexer.IndentWidthCalculator;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.PopulatedAnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.SymbolFileSymbols;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.util.SExpression;
import aldor.util.SymbolPolicy;
import aldor.util.sexpr.SExpressionReadException;
import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharSequenceSubSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adds annotation information onto a file.
 */
public class TypeAndRefAnnotator extends ExternalAnnotator<TypeAndRefAnnotator.CollectedInfo, TypeAndRefAnnotator.FullInfo> {
    private static final Logger LOG = Logger.getInstance(TypeAndRefAnnotator .class);
    private final IndentWidthCalculator calc = new IndentWidthCalculator();

    public static final int COMPILE_TIMEOUT = 5;

    public static class CollectedInfo {
        private final Editor e;
        private final VirtualFile file;
        private final Project project;
        @Nullable
        private final String errorMessage;

        private CollectedInfo(@Nullable String errorMessage, Project project, Editor e, VirtualFile file) {
            this.project = project;
            this.e = e;
            this.file = file;
            this.errorMessage = errorMessage;
        }

        public CollectedInfo(String s) {
            this(s, null, null, null);
        }

        public CollectedInfo(Project project, Editor e, VirtualFile file) {
            this(null, project, e, file);
        }

        public Project project() {
            return project;
        }

        public VirtualFile file() {
            return file;
        }

        public String errorMessage() {
            return errorMessage;
        }

        @Nullable
        public String buildPath() {
            if (file == null) {
                throw new IllegalStateException("buildPath called with no file");
            }
            String filePath = file.getPath();

            Stream<Module> modules = Arrays.stream(ModuleManager.getInstance(project).getModules());
            Stream<VirtualFile> roots = modules.flatMap(module -> Arrays.stream(ModuleRootManager.getInstance(module).getContentRoots()));

            List<File> files = roots.map(f -> new File(f.getPath())).collect(Collectors.toList());
            File buildDir = BuildFiles.buildDirectoryForFile(files, new File(filePath));

            return new File(buildDir, file.getName().substring(0, file.getName().length() - ".as".length()) + ".abn").toString();
        }

        public Editor e() {
            return e;
        }
    }

    public static class FullInfo {
        private final CollectedInfo collectedInfo;
        private final String errorMessage;
        private final PopulatedAnnotationFile annotationFile;

        private FullInfo(CollectedInfo collectedInfo, String errorMessage, PopulatedAnnotationFile annotation) {
            this.collectedInfo = collectedInfo;
            this.errorMessage = errorMessage;
            annotationFile = annotation;
        }

        public FullInfo(CollectedInfo collectedInfo, String errorMessage) {
            this(collectedInfo, errorMessage, null);
        }

        public FullInfo(CollectedInfo collectedInfo, PopulatedAnnotationFile annotation) {
            this(collectedInfo, null, annotation);
        }

        public String errorMessage() {
            return errorMessage;
        }

        public CollectedInfo collectedInfo() {
            return collectedInfo;
        }

        public PopulatedAnnotationFile annotationFile() {
            return annotationFile;
        }
    }


    @Nullable
    @Override
    public CollectedInfo collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        Project project = file.getProject();
        VirtualFile virtualFile = file.getVirtualFile();
        String basePath = project.getBasePath();
        String filePath = virtualFile.getPath();

        if (basePath == null) {
            return new CollectedInfo("Default project. So, no way to compile.");
        }
        if (!filePath.startsWith(basePath)) {
            return new CollectedInfo("File is not in project. No info for you");
        }
        else {
            return new CollectedInfo(project, editor, virtualFile);
        }
    }

    // This is a bit dodgy, as several things may want to use the .abn file.
    @Nullable
    @Override
    public FullInfo doAnnotate(CollectedInfo collectedInfo) {
        if (collectedInfo.errorMessage() != null) {
            return new FullInfo(collectedInfo, collectedInfo.errorMessage());
        }
        final VirtualFile file = collectedInfo.file();
        final VirtualFileSystem vfs = file.getFileSystem();
        final BlockingQueue<CompilerResult> resultLater = new ArrayBlockingQueue<>(1);

        ApplicationManager.getApplication().invokeLater(() -> {
            CompilerManager compilerManager = CompilerManager.getInstance(collectedInfo.project());
            compilerManager.compile(new VirtualFile[]{collectedInfo.file()}, new CompileStatusNotification() {
                @Override
                public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                    resultLater.add(new CompilerResult(aborted, errors, warnings));
                }
            });
        });


        try {
            String buildPath = collectedInfo.buildPath();
            LOG.info("Reading file: " + buildPath);
            assert buildPath != null;
            CompilerResult result = resultLater.poll(COMPILE_TIMEOUT, TimeUnit.SECONDS);
            if (result == null) {
                return new FullInfo(collectedInfo, "Time out while compiling " + file.getPresentableName());
            }

            VirtualFile buildFile = vfs.findFileByPath(buildPath);
            if (buildFile == null) {
                return new FullInfo(collectedInfo, "Compile succeeded, but file apparently doesn't exist (" + buildPath + ")");
            }
            else if (!buildFile.exists()) {
                return new FullInfo(collectedInfo, "Compile succeeded, but file " + buildFile + " not created");
            }
            PopulatedAnnotationFile annotation;
            //noinspection NestedTryStatement
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildFile.getInputStream(), StandardCharsets.US_ASCII))) {
                annotation = new PopulatedAnnotationFile(file.getPath(), SExpression.read(reader, SymbolPolicy.ALLCAPS));
            }
            return new FullInfo(collectedInfo, annotation);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return new FullInfo(collectedInfo, "Interrupt while waiting for the build");
        } catch (IOException e) {
            return new FullInfo(collectedInfo, "Failed to read annotation file: " + e.getMessage());
        }
        catch (SExpressionReadException e) {
            return new FullInfo(collectedInfo, "Failed to parse annotation file: "+ e.getMessage());
        }
    }

    private static final class CompilerResult {
        private final boolean aborted;
        private final int errors;
        private final int warnings;


        CompilerResult(boolean aborted, int errors, int warnings) {
            this.aborted = aborted;
            this.errors = errors;
            this.warnings = warnings;
        }
    }

    @Override
    public void apply(@NotNull PsiFile file, FullInfo annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult.errorMessage() != null) {
            // Log an error here...
            System.out.println("Error: " + file.getName() + ": " + annotationResult.errorMessage());
            return;
        }
        assert annotationResult.annotationFile() != null;
        PopulatedAnnotationFile annotationFile = annotationResult.annotationFile();
        Editor editor = annotationResult.collectedInfo().e();

        for (Map.Entry<SrcPos, SExpression> ent: annotationFile.entries()) {
            PsiElement element = psiElementForSrcPos(file, editor.getDocument(), ent.getKey());
            Map<SExpression, SExpression> idProperties = ent.getValue().cdr().asAssociationList();
            if (element == null) {
                continue;
            }
            TextRange range = element.getTextRange();
            Annotation typeAnnotation = holder.createInfoAnnotation(range, "");
            typeAnnotation.setTextAttributes(JavaHighlightingColors.ANNOTATION_NAME_ATTRIBUTES); //FIXME: Not Java
            SExpression ref = idProperties.get(SymbolFileSymbols.Syme);
            if (ref == null) {
                continue;
            }
            int index = ref.cdr().integer();

            Syme syme = annotationFile.syme(index);
            Syntax syntax = syme.type();
            if (syntax != null) {
                typeAnnotation.setTooltip("<pre>" + syntax + "</pre>");
            }
       }

    }

    @Nullable
    private PsiElement psiElementForSrcPos(PsiFile file, @NotNull Document doc, @NotNull SrcPos srcpos) {
        int lineCount = doc.getLineCount();
        if (lineCount <= srcpos.lineNumber()) {
            return null;
        }
        int startOffset = doc.getLineStartOffset(srcpos.lineNumber()-1);
        int columnOffset = calc.offsetForWidth(new CharSequenceSubSequence(doc.getText(), startOffset, doc.getTextLength()), srcpos.columnNumber());
        return PsiTreeUtil.findElementOfClassAtOffset(file, startOffset + columnOffset, AldorIdentifier.class, true);
    }
}

