package aldor.editor;

import aldor.build.AldorCompilationService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompileMessageAnnotator extends ExternalAnnotator<CompileMessageAnnotator.FileInfo, CompileMessageAnnotator.CompileResult> {

    @Override
    public FileInfo collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new FileInfo(file.getProject(), file.getVirtualFile());
    }

    @Override
    @Nullable
    public CompileResult doAnnotate(FileInfo collectedInfo) {
        AldorCompilationService.getAldorCompilationService(collectedInfo.project()).compilationResultsFor(collectedInfo.file());
        return null;
    }

    @Override
    public void apply(@NotNull PsiFile file, CompileResult annotationResult, @NotNull AnnotationHolder holder) {
    }

    public static class FileInfo {
        private final Project project;
        private final VirtualFile file;

        public FileInfo(Project project, VirtualFile file) {
            this.project = project;
            this.file = file;
        }

        public Project project() {
            return project;
        }

        public VirtualFile file() {
            return file;
        }
    }

    public static class CompileResult {
    }
}
