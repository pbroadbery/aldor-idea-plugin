package aldor.builder.files;

import aldor.builder.AldorTargetIds;
import com.intellij.openapi.util.io.FileUtilRt;
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
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

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
    private final File buildDirectory;
    private final boolean isLocalSdk;

    public AldorFileBuildTarget(AldorFileBuildTargetType targetType,
                                JpsModuleSourceRoot sourceRoot, @NotNull File sourceFile) {
        super(targetType);
        this.sourceFile = sourceFile;
        this.sourceRoot = sourceRoot.getFile();
        this.makeTargetName = BuildFiles.buildTargetName(sourceRoot.getFile(), sourceFile);
        this.buildDirectory = sourceRoot.getFile();
        this.isLocalSdk = false;
    }

    public AldorFileBuildTarget(AldorFileBuildTargetType targetType,
                                JpsModuleSourceRoot sourceRoot, @NotNull File sourceFile, @NotNull File outputDir) {
        super(targetType);
        this.sourceFile = sourceFile;
        this.sourceRoot = sourceRoot.getFile();
        this.makeTargetName = BuildFiles.localBuildTargetName(sourceRoot.getFile(), sourceFile);
        this.buildDirectory = new File(outputDir, FileUtilRt.getRelativePath(sourceRoot.getFile(), sourceFile.getParentFile()));
        this.isLocalSdk = true;
    }

    @Override
    public String toString() {
        return "{FileTarget: " + makeTargetName +"}";
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
        if (isLocalSdk) {
            return Collections.singletonList(this.buildDirectory);
        } else {
            return Collections.singletonList(new File(sourceFile.getParentFile(), "out/ao")); // TODO: Move to BuildFiles
        }
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
