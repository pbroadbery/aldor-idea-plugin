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

import static java.util.stream.Collectors.toList;

/**
 * Created by pab on 29/09/16.
 */
public class AldorModuleBuildTarget extends BuildTarget<AldorRootDescriptor> {
    private static final Logger LOG = Logger.getInstance(AldorModuleBuildTarget.class);
    private final String moduleName;

    public AldorModuleBuildTarget(AldorModuleBuildTargetType type, String moduleName) {
        super(type);
        this.moduleName = moduleName;
    }

    @Override
    public String getId() {
        return moduleName;
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        List<AldorSourceRootBuildTarget> sourceRootTargets = targetRegistry.getAllTargets(AldorBuildTargetTypes.instance.sourceRootTargetType);

        return sourceRootTargets.stream().filter(rootTgt -> rootTgt.moduleName().equals(moduleName)).collect(toList());
    }

    @NotNull
    @Override
    public List<AldorRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index, IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public AldorRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        LOG.warn("find root descriptor for module " + this);
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "aldor-build-module:" + moduleName;
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        return Collections.emptyList();
    }
}
