package aldor.builder.files;

import aldor.builder.AldorTargetIds;
import org.jetbrains.annotations.Contract;
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
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.jps.util.JpsPathUtil.urlToFile;

public class AldorFileBuildTarget extends BuildTarget<AldorFileRootDescriptor> {
    private final AldorFileRootDescriptor rootDescriptor;
    private final File outputLocation;

    public AldorFileBuildTarget(AldorFileBuildTargetType type, JpsModule module, File file) {
        super(type);
        rootDescriptor = createRootDescriptor(module, file);
        File sourceLocation = urlToFile(module.getContentRootsList().getUrls().get(0));

        File destination = new File(sourceLocation.getParentFile(), "build");
        outputLocation = new File(destination, trimExtension(file.getName()) + ".abn"); //FIXME Should be whole path
        assert rootDescriptor.getRootId().equals(file.getPath());
    }

    @NotNull
    @Contract(pure = true)
    public static String trimExtension(@NotNull String name) {
        int index = name.lastIndexOf('.');
        return (index < 0) ? name : name.substring(0, index);
    }

    public AldorFileBuildTargetType getAldorTargetType() {
        return (AldorFileBuildTargetType) getTargetType();
    }


    private AldorFileRootDescriptor createRootDescriptor(JpsModule module, File file) {
        return new AldorFileRootDescriptor(this, module, file);
    }

    @Override
    public String getId() {
        return AldorTargetIds.aldorFileTargetId(rootDescriptor.getRootId());
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<AldorFileRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index,
                                                                IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
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
        return "Aldor-build-file " + rootDescriptor.getSourceFile().getName();
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        return Collections.singletonList(outputLocation);
    }

    File outputLocation() {
        return outputLocation;
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

    public String targetForFile(String name) {
        String trimmedName = trimExtension(name);
        return trimmedName + ".abn";
    }
}
