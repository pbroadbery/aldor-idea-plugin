package aldor.builder.jars;

import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;

import java.io.File;

public class AldorJarRootDescriptor extends BuildRootDescriptor {
    private final File sourceRoot;
    private final BuildTarget<?> target;

    public AldorJarRootDescriptor(File sourceRoot, BuildTarget<?> target) {
        this.sourceRoot = sourceRoot;
        this.target = target;
    }

    @Override
    public String getRootId() {
        return sourceRoot + "-jar";
    }

    @Override
    public File getRootFile() {
        return sourceRoot;
    }

    @Override
    public BuildTarget<?> getTarget() {
        return target;
    }
}
