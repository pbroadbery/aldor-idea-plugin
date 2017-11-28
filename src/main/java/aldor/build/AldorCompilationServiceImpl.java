package aldor.build;

import com.google.common.collect.Maps;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerTopics;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AldorCompilationServiceImpl extends AldorCompilationService {
    private static final Logger LOG = Logger.getInstance(AldorCompilationServiceImpl.class);
    private final Map<String, CompileState> compileStateForFile;

    public AldorCompilationServiceImpl(@SuppressWarnings("TypeMayBeWeakened") @NotNull Project project) {
        LOG.info("New Compiler!");
        this.compileStateForFile = Maps.newHashMap();

        final MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(CompilerTopics.COMPILATION_STATUS, new AldorCompilationServiceListener());
    }

    @Override
    public void compilationResultsFor(VirtualFile file) {
        LOG.info("Getting compile results for: " + file);
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
