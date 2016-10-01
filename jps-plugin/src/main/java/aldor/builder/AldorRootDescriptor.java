package aldor.builder;

import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;

import java.io.File;

/**
 * Root descriptor for aldor sources.
 */
public class AldorRootDescriptor extends BuildRootDescriptor {
    private final File rootDirectory;
    private final File outputDirectory;
    private final BuildTarget<?> buildTarget;
    private final String moduleName;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    AldorRootDescriptor(AldorSourceRootBuildTargetType type, String moduleName, File rootDirectory, File outputDirectory) {
        this.rootDirectory = rootDirectory;
        this.outputDirectory = outputDirectory;
        this.moduleName = moduleName;
        this.buildTarget = new AldorSourceRootBuildTarget(type, this);
    }

    @Override
    public String getRootId() {
        return rootDirectory.getAbsolutePath();
    }

    @Override
    public File getRootFile() {
        return rootDirectory;
    }

    @Override
    public BuildTarget<?> getTarget() {
        return buildTarget;
    }

    public File outputDirectory() {
        return outputDirectory;
    }

    public String moduleName() {
        return moduleName;
    }
}
