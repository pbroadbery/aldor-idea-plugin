package aldor.build.module;

import com.intellij.psi.PsiFile;

import java.util.concurrent.Future;

public interface AnnotationFileBuilder {

    Future<Void> invokeAnnotationBuild(PsiFile psiFile);

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
