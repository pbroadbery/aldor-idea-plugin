package aldor.build;

import com.google.common.collect.Maps;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AldorCompilationServiceImpl extends AldorCompilationService {
    private static final Logger LOG = Logger.getInstance(AldorCompilationServiceImpl.class);
    private final Map<String, CompileState> compileStateForFile;
    private final Project project;

    public AldorCompilationServiceImpl(@NotNull Project project) {
        LOG.info("New Compiler!");
        this.project = project;
        this.compileStateForFile = Maps.newHashMap();
        CompilerManager.getInstance(project).addCompilationStatusListener(new Listener());
    }

    @Override
    public void compilationResultsFor(VirtualFile file) {
        LOG.info("Getting compile results for: " + file);
    }

    private class Listener implements CompilationStatusListener {

        @Override
        public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
            LOG.info("Compiled ");

        }

        @Override
        public void automakeCompilationFinished(int errors, int warnings, CompileContext compileContext) {
            LOG.info("Auto compile finished " + errors);
        }

        @Override
        public void fileGenerated(String outputRoot, String relativePath) {
            LOG.info("Generated file " + outputRoot +  " " + relativePath);
        }
    }

    private static class CompileState {}

    private static class InProgressCompileState extends CompileState {

    }

    private static class FinishedCompileState extends CompileState {
        private final List<CompilerMessage> messages;

        FinishedCompileState(Collection<CompilerMessage> messages) {
            this.messages = new ArrayList<>(messages);
        }
    }
}
