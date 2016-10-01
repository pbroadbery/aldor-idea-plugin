package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
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

/**
 * Builds a whole directory
 */
public class AldorSourceRootBuildTarget extends BuildTarget<AldorRootDescriptor> {
    private static final Logger LOG = Logger.getInstance(AldorSourceRootBuildTarget.class);
    private final AldorRootDescriptor rootDescriptor;

    protected AldorSourceRootBuildTarget(@NotNull AldorSourceRootBuildTargetType type, @NotNull AldorRootDescriptor rootDescriptor) {
        super(type);
        this.rootDescriptor = rootDescriptor;
    }

    @Override
    public String getId() {
        return rootDescriptor.getRootId();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "AldorSourceRootBuildTarget " + rootDescriptor.getRootId();
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        LOG.info("Compute dependencies: Just files");
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<AldorRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index,
                                                            IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        return Collections.singletonList(rootDescriptor);
    }

    @Nullable
    @Override
    public AldorRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        return rootDescriptor;
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        return Collections.singletonList(rootDescriptor.outputDirectory());
    }

    public String moduleName() {
        return rootDescriptor.moduleName();
    }
}
