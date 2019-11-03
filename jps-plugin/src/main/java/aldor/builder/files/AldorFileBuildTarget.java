package aldor.builder.files;

import aldor.builder.AldorTargetIds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AldorFileBuildTarget extends BuildTarget<AldorFileRootDescriptor> {
    @NotNull
    private final String makeTargetName;
    @NotNull
    private final File sourceFile;
    @NotNull
    private final File sourceRoot;
    @NotNull
    private final File buildDirectory;

    public AldorFileBuildTarget(AldorFileBuildTargetType targetType, @NotNull String makeTargetName, @NotNull File sourceFile, @NotNull File sourceRoot, @NotNull File buildDirectory) {
        super(targetType);
        this.makeTargetName = makeTargetName;
        this.sourceFile = sourceFile;
        this.sourceRoot = sourceRoot;
        this.buildDirectory = buildDirectory;
    }

    @Override
    public String toString() {
        return "{FileTarget: " + makeTargetName + "@" + buildDirectory +"}";
    }

    public AldorFileBuildTargetType getAldorTargetType() {
        return (AldorFileBuildTargetType) getTargetType();
    }

    @NotNull
    public String makeTargetName() {
        return makeTargetName;
    }

    @NotNull
    public File outputLocation() {
        return new File(sourceRoot, makeTargetName());
    }

    @Override
    @NotNull
    public String getId() {
        return AldorTargetIds.aldorFileTargetId(sourceFile.getPath());
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<AldorFileRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index,
                                                                IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        AldorFileRootDescriptor rootDescriptor = new AldorFileRootDescriptor(this, sourceRoot, sourceFile);
        return Collections.singletonList(rootDescriptor);
    }

    @Nullable
    @Override
    public AldorFileRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "{Aldor-build-file " + makeTargetName + "}";
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        return Collections.singletonList(this.buildDirectory);
    }

    public ExecutorService executor() {
        return getAldorTargetType().buildService().executorService();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AldorFileBuildTarget)) {
            return false;
        }
        AldorFileBuildTarget other = (AldorFileBuildTarget) o;
        return other.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public File sourceFile() {
        return sourceFile;
    }

    @NotNull
    public File buildDirectory() {
        return this.buildDirectory;
    }

    public File makeFile() {
        return new File(this.sourceRoot, "Makefile");
    }

    @NotNull
    public File sourceRoot() {
        return this.sourceRoot;
    }

}
