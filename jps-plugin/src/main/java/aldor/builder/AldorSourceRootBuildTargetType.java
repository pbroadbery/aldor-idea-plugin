package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AldorSourceRootBuildTargetType extends BuildTargetType<AldorSourceRootBuildTarget> {
    private static final Logger LOG = Logger.getInstance(AldorSourceRootBuildTargetType.class);

    protected AldorSourceRootBuildTargetType() {
        super(AldorBuildConstants.ALDOR_SOURCE_ROOT_TARGET);
    }

    @NotNull
    @Override
    public List<AldorSourceRootBuildTarget> computeAllTargets(@NotNull JpsModel model) {
        LOG.info("Modules: " + model.getProject() + " " + model.getProject().getModules());
        List<AldorSourceRootBuildTarget> targets = new ArrayList<>();
        for (JpsModule module: model.getProject().getModules()) {
            for (JpsModuleSourceRoot elt: module.getSourceRoots()) {
                AldorRootDescriptor rootDescriptor = new AldorRootDescriptor(this, module.getName(), elt.getFile(),
                        new File(elt.getFile().getParentFile(),  "build"));
                targets.add(new AldorSourceRootBuildTarget(this, rootDescriptor));
            }
        }
        LOG.info("Targets are: " + targets.stream().map(AldorSourceRootBuildTarget::getPresentableName).collect(Collectors.toList()));
        return targets;
    }

    @NotNull
    @Override
    public BuildTargetLoader<AldorSourceRootBuildTarget> createLoader(@NotNull final JpsModel model) {
        return AldorBuildTargetTypes.instance.createLoader(this, model);
    }

    @Override
    public String toString() {
        return "AldorTargetType";
    }
}
