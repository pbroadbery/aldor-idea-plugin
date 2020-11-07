package aldor.builder.jars;

import aldor.builder.AldorBuilderService;
import aldor.builder.AldorTargetIds;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
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
import java.util.stream.Collectors;

public class AldorJarBuildTarget extends BuildTarget<AldorJarRootDescriptor> {
    private static final Logger LOG = Logger.getInstance(AldorJarBuildTarget.class);
    private final JpsModuleSourceRoot sourceRoot;
    private final AldorBuilderService builderService;

    public AldorJarBuildTarget(AldorJarBuildTargetType jarBuildTargetType, JpsModuleSourceRoot sourceRoot) {
        super(jarBuildTargetType);
        this.sourceRoot = sourceRoot;
        this.builderService = jarBuildTargetType.buildService();
        LOG.info("Created jar build target " + getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AldorJarBuildTarget other = (AldorJarBuildTarget) obj;
        return FileUtil.filesEqual(this.sourceRoot.getFile(), other.sourceRoot.getFile());
    }

    @Override
    public int hashCode() {
        return FileUtil.fileHashCode(sourceRoot.getFile());
    }

    @Override
    public String toString() {
        return "{JarTarget: " + sourceRoot +"}";
    }

    @Override
    public String getId() {
        return AldorTargetIds.aldorJarTargetId(sourceRoot);
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        List<BuildTarget<?>> dependencies = targetRegistry.getAllTargets(builderService.targetTypes().fileBuildTargetType).stream()
                .filter(tgt -> FileUtil.filesEqual(tgt.sourceRoot(), sourceRoot.getFile()))
                .collect(Collectors.toList());
        LOG.info("Dependencies: " + this + " --> " + dependencies);
        return dependencies;
    }

    @NotNull
    @Override
    public List<AldorJarRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index, IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        return Collections.singletonList(new AldorJarRootDescriptor(sourceRoot.getFile(), this));
    }

    @Nullable
    @Override
    public AldorJarRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        LOG.info("Find root descriptor: " + this + " " + rootId);
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
