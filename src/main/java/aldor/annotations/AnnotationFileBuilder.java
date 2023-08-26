package aldor.annotations;

import com.intellij.psi.PsiFile;

import java.util.concurrent.CompletableFuture;

public interface AnnotationFileBuilder {

    CompletableFuture<Void> invokeAnnotationBuild(PsiFile psiFile);

    final class CompilerResult {
        private final boolean aborted;
        private final int errors;
        private final int warnings;


        CompilerResult(boolean aborted, int errors, int warnings) {
            this.aborted = aborted;
            this.errors = errors;
            this.warnings = warnings;
        }
    }

}
