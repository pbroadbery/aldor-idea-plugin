package aldor.builder.files;

import aldor.builder.AldorBuildTargetTypes;
import aldor.builder.AldorBuilderService;
import aldor.builder.jps.JpsAldorExtension;
import aldor.builder.jps.JpsAldorModuleProperties;
import aldor.builder.jps.JpsAldorModuleType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.module.JpsTypedModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return "{AldorFileBuildTargetType}";
    }

    @NotNull
    @Override
    public List<AldorFileBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
        return model.getProject().getModules().stream()
                .map(this::moduleBuildTargets).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @NotNull
    private List<AldorFileBuildTarget> moduleBuildTargets(JpsModule module) {
        JpsTypedModule<JpsSimpleElement<JpsAldorModuleProperties>> aldorModule = module.asTyped(JpsAldorModuleType.INSTANCE);
        if (aldorModule == null) {
            return Collections.emptyList();
        }
        else {
            return aldorModuleBuildTargets(aldorModule);
        }
    }

    @NotNull
    private List<AldorFileBuildTarget> aldorModuleBuildTargets(JpsModule aldorModule) {
        List<String> contentRoots = aldorModule.getContentRootsList().getUrls();
        List<File> rootFiles = contentRoots.stream().filter(url -> url.startsWith("file://")).map(JpsPathUtil::urlToFile).collect(Collectors.toList());
        List<JpsModuleSourceRoot> sourceRoots = aldorModule.getSourceRoots();
        JpsAldorModuleProperties properties = JpsAldorModuleType.INSTANCE.moduleProperties(aldorModule);
        if (!properties.isValid()) {
            LOG.error("Cannot create targets for " + aldorModule.getName());
        }
        LOG.info("Creating build targets");
        List<AldorFileBuildTarget> targets = new ArrayList<>();
        Optional<JpsSdk<?>> localSdk = localSdk(aldorModule);
        for (JpsModuleSourceRoot sourceRoot: sourceRoots) {
            LOG.info("Source root: " + sourceRoot.getFile() + " type: " + sourceRoot.getRootType());
            File file = sourceRoot.getFile();

            Optional<File> maybeRoot = rootFiles.stream().filter(root -> FileUtil.isAncestor(root, file, false)).findFirst();
            if (!maybeRoot.isPresent()) {
                LOG.debug("No root for " + file);
                continue;
            }
            File contentRoot = maybeRoot.get();
            for (File sourceFile: findFilesByMask(SOURCE_FILES, file)) {

                String targetName = JpsAldorModuleType.INSTANCE.targetName(properties, sourceRoot.getFile(), sourceFile);
                File buildDirectory = JpsAldorModuleType.INSTANCE.buildDirectory(properties, contentRoot, sourceRoot.getFile(), sourceFile);
                final AldorFileBuildTarget target = new AldorFileBuildTarget(this, targetName,
                        sourceFile, sourceRoot.getFile(),
                        buildDirectory);

                LOG.info("Adding target " + target);
                targets.add(target);
            }

        }
        LOG.info("Created " + targets.size());
        return targets;
    }

    private Optional<JpsSdk<?>> localSdk(JpsModule module) {
        JpsSdk<JpsDummyElement> sdk = module.getSdk(JpsAldorExtension.JpsAldorSdkType.LOCAL);
        return Optional.ofNullable(sdk);
    }

    /*@NotNull
    private Stream<AldorFileBuildTarget> moduleBuildTargets_oldversion(JpsModule module) {
        List<String> urls = module.getContentRootsList().getUrls();
        LOG.info("URLs: " + urls);
        Stream<File> paths = urls.stream().map(JpsPathUtil::urlToFile);

        Stream<File> files = paths.flatMap(path -> findFilesByMask(SOURCE_FILES, path).stream());

        return files.map(file -> new AldorFileBuildTarget(this, module, file));
    }*/

    @NotNull
    @Override
    public BuildTargetLoader<AldorFileBuildTarget> createLoader(@NotNull JpsModel model) {
        return AldorBuildTargetTypes.createLoader(this, model);
    }

    public AldorBuilderService buildService() {
        return buildService;
    }

}
