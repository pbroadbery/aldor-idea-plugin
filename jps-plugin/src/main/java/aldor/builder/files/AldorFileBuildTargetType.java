package aldor.builder.files;

import aldor.builder.AldorBuilderService;
import aldor.builder.AldorTargetIds;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.builder.AldorBuildConstants.ALDOR_FILE_TARGET;
import static com.intellij.openapi.util.io.FileUtil.findFilesByMask;
import static org.jetbrains.jps.util.JpsPathUtil.urlToFile;

public class AldorFileBuildTargetType extends BuildTargetType<AldorFileBuildTargetType.AldorFileBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorFileBuildTargetType.class);
    private final AldorBuilderService buildService;

    public AldorFileBuildTargetType(AldorBuilderService service) {
        super(ALDOR_FILE_TARGET);
        this.buildService = service;
    }

    private static final Pattern SOURCE_FILES = Pattern.compile(".*\\.as");

    @NotNull
    @Override
    public List<AldorFileBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
        JpsModule module = model.getProject().getModules().get(0);

        List<String> urls = module.getContentRootsList().getUrls();

        LOG.info("URLs: " + urls);
        Stream<File> paths = urls.stream().map(JpsPathUtil::urlToFile);

        Stream<File> files = paths.flatMap(path -> findFilesByMask(SOURCE_FILES, path).stream());

        Stream<AldorFileBuildTarget> targets = files.map(file -> new AldorFileBuildTarget(this, module, file));
        List<AldorFileBuildTarget> targetList = targets.collect(Collectors.toList());

        LOG.info("Created " + targetList.size() + " targets");
        return targetList;
    }

    @NotNull
    @Override
    public BuildTargetLoader<AldorFileBuildTarget> createLoader(@NotNull JpsModel model) {
        return buildService.targetTypes().createLoader(this, model);
    }

    public static ExecutorService executorFor(AldorFileRootDescriptor descriptor) {
        return descriptor.getTarget().getAldorTargetType().buildService().executorService();
    }

    private AldorBuilderService buildService() {
        return buildService;
    }

    public static class AldorFileBuildTarget extends BuildTarget<AldorFileRootDescriptor> {
        private final AldorFileRootDescriptor rootDescriptor;
        private final File outputLocation;

        public AldorFileBuildTarget(AldorFileBuildTargetType type, JpsModule module, File file) {
            super(type);
            rootDescriptor = createRootDescriptor(module, file);
            File sourceLocation = urlToFile(module.getContentRootsList().getUrls().get(0));

            File destination = new File(sourceLocation.getParentFile(), "build");
            outputLocation = new File(destination, StringUtil.trimExtension(file.getName()) + ".abn"); //FIXME Should be whole path
            assert rootDescriptor.getRootId().equals(file.getPath());
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
            String trimmedName = StringUtil.trimExtension(name);
            return trimmedName + ".abn";
        }
    }

}
