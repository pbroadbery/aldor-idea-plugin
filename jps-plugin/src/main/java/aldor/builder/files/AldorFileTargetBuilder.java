package aldor.builder.files;

import aldor.make.FullCompileRunner;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

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
                      @NotNull final DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder,
                      @NotNull final BuildOutputConsumer outputConsumer, @NotNull final CompileContext context) throws ProjectBuildException, IOException {
        LOG.info("Building " + target + " " + holder.hasDirtyFiles());

        CompileRunner compiler = CompileRunner.logged(new FullCompileRunner(target.executor(), context));
        /*holder.processDirtyFiles((target1, file, descriptor) -> {
            outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(descriptor.getRootFile().toString()));
            return compiler.compileOneFile(target1, file, descriptor);
        });*/

        outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(target.sourceFile().getPath()));
        outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(target.makeFile().getPath()));
        File buildDirectory = target.buildDirectory();
        String targetName = target.makeTargetName();

        if (compiler.compileOneFile(buildDirectory, targetName)) {
            LOG.info(" .. Built " + target);
        } else {
            throw new ProjectBuildException("Failed to compile " + targetName + " in " + buildDirectory);
        }
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Aldor-file-target-builder";
    }

}
