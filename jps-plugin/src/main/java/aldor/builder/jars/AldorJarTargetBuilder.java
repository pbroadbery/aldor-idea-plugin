package aldor.builder.jars;

import aldor.builder.files.AldorFileTargetBuilder;
import aldor.make.FullCompiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.CompileScope;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

// TODO: Try to combine this and AldorFileTargetBuilder
public class AldorJarTargetBuilder extends TargetBuilder<AldorJarRootDescriptor, AldorJarBuildTarget> {

    public AldorJarTargetBuilder(AldorJarBuildTargetType jarBuildTargetType) {
        super(Collections.singletonList(jarBuildTargetType));
    }

    @Override
    public void build(@NotNull AldorJarBuildTarget target, @NotNull DirtyFilesHolder<AldorJarRootDescriptor, AldorJarBuildTarget> holder,
                      @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException, IOException {

        CompileScope scope = context.getScope();

        AldorFileTargetBuilder.Compiler compiler = new FullCompiler(target.builderService(), context);
        /*holder.processDirtyFiles((target1, file, descriptor) -> {
            outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(descriptor.getRootFile().toString()));
            return compiler.compileOneFile(target1, file, descriptor);
        });*/

        //outputConsumer.registerOutputFile(target.outputLocation(), Collections.singletonList(target.makeFile().getPath()));
        File buildDirectory = target.buildDirectory();
        String targetName = target.jarFileTarget();

        if (!compiler.compileOneFile(buildDirectory, targetName)) {
            throw new ProjectBuildException("Failed to compile " + targetName + " in " + buildDirectory);
        }

    }


    @NotNull
    @Override
    public String getPresentableName() {
        return "aldor-jar-builder";
    }
}
