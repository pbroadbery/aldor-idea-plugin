package aldor.builder.jars;

import aldor.builder.AldorBuildTargetTypes;
import aldor.builder.AldorBuilderService;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.builder.AldorBuildConstants.ALDOR_JAR_TARGET;

public class AldorJarBuildTargetType extends BuildTargetType<AldorJarBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorJarBuildTargetType.class);
    private final AldorBuilderService buildService;

    public AldorJarBuildTargetType(AldorBuilderService service) {
        super(ALDOR_JAR_TARGET);
        this.buildService = service;
    }

    @NotNull
    @Override
    public List<AldorJarBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
        return model.getProject().getModules().stream()
                .map(this::moduleBuildTargets).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @NotNull
    private List<AldorJarBuildTarget> moduleBuildTargets(JpsModule module) {
        List<JpsModuleSourceRoot> sourceRoots = module.getSourceRoots();

        return sourceRoots.stream().map(sourceRoot -> new AldorJarBuildTarget(this, sourceRoot)).collect(Collectors.toList());
    }


    @NotNull
    @Override
    public BuildTargetLoader<AldorJarBuildTarget> createLoader(@NotNull JpsModel model) {
        return AldorBuildTargetTypes.createLoader(this, model);
    }

    public AldorBuilderService buildService() {
        return buildService;
    }

}
