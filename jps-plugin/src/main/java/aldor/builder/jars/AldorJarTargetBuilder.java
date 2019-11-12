package aldor.builder.jars;

import aldor.builder.files.AldorFileTargetBuilder;
import aldor.make.FullCompiler;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.CompileScope;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.File;
import java.util.Collections;

// TODO: Try to combine this and AldorFileTargetBuilder
public class AldorJarTargetBuilder extends TargetBuilder<AldorJarRootDescriptor, AldorJarBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorJarTargetBuilder.class);

    public AldorJarTargetBuilder(AldorJarBuildTargetType jarBuildTargetType) {
        super(Collections.singletonList(jarBuildTargetType));
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
    public void build(@NotNull AldorJarBuildTarget target, @NotNull DirtyFilesHolder<AldorJarRootDescriptor, AldorJarBuildTarget> holder,
                      @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException {

        CompileScope scope = context.getScope();
        LOG.info("Jar build starts " + target);
        AldorFileTargetBuilder.Compiler compiler = new FullCompiler(target.builderService(), context);
        File buildDirectory = target.buildDirectory();
        String targetName = target.jarFileTarget();

        if (!compiler.compileOneFile(buildDirectory, targetName)) {
            throw new ProjectBuildException("Failed to compile " + targetName + " in " + buildDirectory);
        }
        LOG.info("Jar build ends " + target);
    }


    @NotNull
    @Override
    public String getPresentableName() {
        return "aldor-jar-builder";
    }
}
