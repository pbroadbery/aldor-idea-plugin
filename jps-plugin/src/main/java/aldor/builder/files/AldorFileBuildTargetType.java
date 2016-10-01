package aldor.builder.files;

import aldor.builder.AldorBuildTargetTypes;
import aldor.builder.AldorTargetIds;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.builder.AldorBuildConstants.ALDOR_FILE_TARGET;
import static com.intellij.openapi.util.io.FileUtil.findFilesByMask;
import static org.jetbrains.jps.util.JpsPathUtil.urlToFile;

public class AldorFileBuildTargetType extends BuildTargetType<AldorFileBuildTargetType.AldorFileBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorFileBuildTargetType.class);

    public AldorFileBuildTargetType() {
        super(ALDOR_FILE_TARGET);
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
        return AldorBuildTargetTypes.instance.createLoader(this, model);
    }

    public static class AldorFileBuildTarget extends BuildTarget<AldorFileRootDescriptor> {
        private final AldorFileRootDescriptor rootDescriptor;
        private final File outputLocation;

        public AldorFileBuildTarget(AldorFileBuildTargetType type, JpsModule module, File file) {
            super(type);
            rootDescriptor = createRootDescriptor(module, file);
            File sourceLocation = urlToFile(module.getContentRootsList().getUrls().get(0));
            File destination = new File(sourceLocation.getParentFile(), "build");
            outputLocation = new File(destination, file.getName() + ".inf"); //FIXME Should be whole path
            assert rootDescriptor.getRootId().equals(file.getPath());
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
            return "Aldor-build-file " + rootDescriptor.getFile().getName();
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

    }

    public static class AldorFileTargetBuilder extends TargetBuilder<AldorFileRootDescriptor, AldorFileBuildTarget> {
        private static final Logger LOG = Logger.getInstance(AldorFileTargetBuilder.class);

        public AldorFileTargetBuilder(AldorFileBuildTargetType type) {
            super(Collections.singletonList(type));
        }

        @Override
        public void buildStarted(CompileContext context) {
            LOG.info("Build started");
        }


        @Override
        public void buildFinished(CompileContext context) {
            LOG.info("Build started");
        }


        @Override
        public void build(@NotNull AldorFileBuildTarget target,
                          @NotNull final DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder,
                          @NotNull final BuildOutputConsumer outputConsumer, @NotNull final CompileContext context) throws ProjectBuildException, IOException {

            LOG.info("Building " + target + " " + holder.hasDirtyFiles());

            LocalCompiler compiler = new LocalCompiler(holder, outputConsumer, context);
            holder.processDirtyFiles(compiler::compileOneFile);
        }

        @NotNull
        @Override
        public String getPresentableName() {
            return "Aldor-file-target-builder";
        }

        private static class LocalCompiler {
            private final DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder;
            private final BuildOutputConsumer outputConsumer;
            private final CompileContext context;

            LocalCompiler(DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder, BuildOutputConsumer outputConsumer, CompileContext context) {
                this.holder = holder;
                this.outputConsumer = outputConsumer;
                this.context = context;
            }

            @SuppressWarnings("SameReturnValue")
            private boolean compileOneFile(AldorFileBuildTarget target, File file, AldorFileRootDescriptor root) {
                boolean created = target.outputLocation().getParentFile().mkdirs();
                if (!target.outputLocation().exists() && target.outputLocation().canWrite()) {
                    LOG.error("Can't write to file: " + target.outputLocation());
                }
                else {
                    try {
                        target.outputLocation().createNewFile();
                        context.processMessage(new CompilerMessage("aldor builder", BuildMessage.Kind.INFO,
                                "created file " + target.outputLocation()));
                    } catch (IOException e) {
                        LOG.error("Failed to create output file");
                    }
                }
                return true;
            }

        }
    }

}
