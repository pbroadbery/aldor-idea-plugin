package aldor.builder.files;

import aldor.make.FullCompiler;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Builder for aldor files - really just knows about .abn files.
 */
public class AldorFileTargetBuilder extends TargetBuilder<AldorFileRootDescriptor, AldorFileBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorFileTargetBuilder.class);

    public AldorFileTargetBuilder(AldorFileBuildTargetType type) {
        super(Collections.singletonList(type));
    }

    @Override
    public void buildStarted(CompileContext context) {
        LOG.info("Build started");
    }


    @Override
    public void buildFinished(CompileContext context) {
        LOG.info("Build finished");
    }


    @Override
    public void build(@NotNull final AldorFileBuildTarget target,
                      final DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder,
                      @NotNull final BuildOutputConsumer outputConsumer, @NotNull final CompileContext context) throws ProjectBuildException, IOException {
        LOG.info("Building " + target + " " + holder.hasDirtyFiles());

        Compiler compiler = new FullCompiler(holder, outputConsumer, context);
        holder.processDirtyFiles((target1, file, descriptor) -> {
            outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(descriptor.getRootFile().toString()));
            return compiler.compileOneFile(target1, file, descriptor);
        });
      }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Aldor-file-target-builder";
    }

    public interface Compiler {
        boolean compileOneFile(AldorFileBuildTarget target, File file, AldorFileRootDescriptor descriptor);
    }

    private static class LocalCompiler implements Compiler {
        private final DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder;
        private final BuildOutputConsumer outputConsumer;
        private final CompileContext context;

        LocalCompiler(DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder, BuildOutputConsumer outputConsumer, CompileContext context) {
            this.holder = holder;
            this.outputConsumer = outputConsumer;
            this.context = context;
        }

        @Override
        @SuppressWarnings("SameReturnValue")
        public boolean compileOneFile(AldorFileBuildTarget target, File file, AldorFileRootDescriptor root) {
            boolean created = target.outputLocation().getParentFile().mkdirs();
            if (created) {
                LOG.info("Created output location: " + target.outputLocation().getParentFile());
            }
            if (!target.outputLocation().exists() && target.outputLocation().canWrite()) {
                LOG.error("Can't write to file: " + target.outputLocation());
            } else {
                try {
                    boolean newFile = target.outputLocation().createNewFile();
                    if (newFile) {
                        LOG.info("created new file: " + target.outputLocation());
                    }
                    context.processMessage(new CompilerMessage("aldor builder", BuildMessage.Kind.INFO,
                            "created file " + target.outputLocation()));
                } catch (IOException e) {
                    LOG.error("Failed to create output file", e);
                }
            }
            return true;
        }

    }
}
