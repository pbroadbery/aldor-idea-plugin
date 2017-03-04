package aldor.builder.files;

import aldor.builder.AldorBuildTargetTypes;
import aldor.builder.AldorBuilderService;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.builder.AldorBuildConstants.ALDOR_FILE_TARGET;
import static com.intellij.openapi.util.io.FileUtil.findFilesByMask;

public class AldorFileBuildTargetType extends BuildTargetType<AldorFileBuildTarget> {
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

        return model.getProject().getModules().stream()
                .flatMap(this::moduleBuildTargets).collect(Collectors.toList());
    }

    @NotNull
    private Stream<AldorFileBuildTarget> moduleBuildTargets(JpsModule module) {
        List<String> urls = module.getContentRootsList().getUrls();

        LOG.info("URLs: " + urls);
        Stream<File> paths = urls.stream().map(JpsPathUtil::urlToFile);

        Stream<File> files = paths.flatMap(path -> findFilesByMask(SOURCE_FILES, path).stream());

        return files.map(file -> new AldorFileBuildTarget(this, module, file));
    }

    @NotNull
    @Override
    public BuildTargetLoader<AldorFileBuildTarget> createLoader(@NotNull JpsModel model) {
        return AldorBuildTargetTypes.createLoader(this, model);
    }

    public static ExecutorService executorFor(AldorFileRootDescriptor descriptor) {
        return descriptor.getTarget().getAldorTargetType().buildService().executorService();
    }

    private AldorBuilderService buildService() {
        return buildService;
    }

}
