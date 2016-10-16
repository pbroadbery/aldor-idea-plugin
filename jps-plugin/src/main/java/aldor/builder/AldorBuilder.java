package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.IOException;
import java.util.Collections;

public class AldorBuilder extends TargetBuilder<AldorRootDescriptor, AldorSourceRootBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorBuilder.class);

    protected AldorBuilder(AldorSourceRootBuildTargetType targetType) {
        super(Collections.singletonList(targetType));
    }


    @NotNull
    @Override
    public String getPresentableName() {
        return "Aldor Builder";
    }

    @Override
    public void build(@NotNull AldorSourceRootBuildTarget target, @NotNull DirtyFilesHolder<AldorRootDescriptor, AldorSourceRootBuildTarget> holder,
                      @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException, IOException {
        System.out.println("Building target");
        LOG.info("Building " + target);
        context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "Build for " + target + " complete"));
    }
}
