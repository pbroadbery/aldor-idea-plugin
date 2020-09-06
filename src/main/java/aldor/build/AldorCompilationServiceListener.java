package aldor.build;

import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

class AldorCompilationServiceListener implements CompilationStatusListener {
    private static final Logger LOG = Logger.getInstance(AldorCompilationServiceListener.class);

    @Override
    public void compilationFinished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        LOG.info("Compiled ");

    }

    @Override
    public void automakeCompilationFinished(int errors, int warnings, @NotNull CompileContext compileContext) {
        LOG.info("Auto compile finished " + errors);
    }

    @Override
    public void fileGenerated(@NotNull String outputRoot, @NotNull String relativePath) {
        LOG.info("Generated file " + outputRoot + " " + relativePath);
    }
}