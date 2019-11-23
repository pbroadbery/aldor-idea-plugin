package aldor.builder.jars;

import aldor.builder.AldorBuildTargetTypes;
import aldor.builder.AldorBuilderService;
import aldor.builder.jps.JpsAldorExtension;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.builder.AldorBuildConstants.ALDOR_JAR_TARGET;

public class  AldorJarBuildTargetType extends BuildTargetType<AldorJarBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorJarBuildTargetType.class);
    private final AldorBuilderService buildService;

    public AldorJarBuildTargetType(AldorBuilderService service) {
        super(ALDOR_JAR_TARGET);
        this.buildService = service;
    }

    @Override
    public String toString() {
        return "{AldorJarBuildTargetType}";
    }


    @NotNull
    @Override
    public List<AldorJarBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
        LOG.info("Modules: "+ model.getProject().getModules());
        List<AldorJarBuildTarget> targets = model.getProject().getModules().stream()
                .map(this::moduleBuildTargets).flatMap(Collection::stream).collect(Collectors.toList());
        LOG.info("Created " + targets.size() + " targets " + targets.stream().map(AldorJarBuildTarget::getId).collect(Collectors.joining(",")));
        return targets;
    }

    @NotNull
    private List<AldorJarBuildTarget> moduleBuildTargets(JpsModule module) {
        LOG.info("Module: "+ module.getSdk(JpsAldorExtension.JpsAldorSdkType.INSTALLED));
        if (module.getSdk(JpsAldorExtension.JpsAldorSdkType.LOCAL) != null) {
            return Collections.emptyList();
        }
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
