package aldor.builder.jars;

import aldor.builder.AldorBuilderService;
import aldor.builder.AldorTargetIds;
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
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AldorJarBuildTarget extends BuildTarget<AldorJarRootDescriptor> {
    private static final Logger LOG = Logger.getInstance(AldorJarBuildTarget.class);
    private final JpsModuleSourceRoot sourceRoot;
    private final AldorBuilderService builderService;

    public AldorJarBuildTarget(AldorJarBuildTargetType jarBuildTargetType, JpsModuleSourceRoot sourceRoot) {
        super(jarBuildTargetType);
        this.sourceRoot = sourceRoot;
        this.builderService = jarBuildTargetType.buildService();
    }

    @Override
    public String getId() {
        return AldorTargetIds.aldorJarTargetId(sourceRoot);
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        /*return targetRegistry.getAllTargets(builderService.targetTypes().fileBuildTargetType).stream()
                .filter(tgt -> FileUtil.filesEqual(tgt.sourceRoot(), sourceRoot.getFile()))
                .collect(Collectors.toList());
        */
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<AldorJarRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index, IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        return Collections.singletonList(new AldorJarRootDescriptor(sourceRoot.getFile(), this));
    }

    @Nullable
    @Override
    public AldorJarRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        LOG.info("Find root descriptor: " + rootId);
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return sourceRoot.getFile() + " - jar file";
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        File outputDirectory = new File(sourceRoot.getFile(), "out/jar");
        return Collections.singletonList(outputDirectory);
    }

    public AldorBuilderService builderService() {
        return builderService;
    }

    public File buildDirectory() {
        return sourceRoot.getFile();
    }

    public String jarFileTarget() {
        return "out/jar/" + sourceRoot.getFile().getName() + ".jar";
    }
}
