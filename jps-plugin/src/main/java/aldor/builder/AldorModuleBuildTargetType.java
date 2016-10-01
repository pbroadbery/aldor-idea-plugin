package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pab on 29/09/16.
 */
public class AldorModuleBuildTargetType extends BuildTargetType<AldorModuleBuildTarget> {

    private static final Logger LOG = Logger.getInstance(AldorModuleBuildTargetType.class);

    protected AldorModuleBuildTargetType() {
        super(AldorBuildConstants.ALDOR_MODULE_TARGET);
    }

    @NotNull
    @Override
    public List<AldorModuleBuildTarget> computeAllTargets(@NotNull JpsModel model) {
        List<AldorModuleBuildTarget> targets = new ArrayList<>();
        for (JpsModule module: model.getProject().getModules()) {
            targets.add(new AldorModuleBuildTarget(this, module.getName()));
        }
        LOG.info("Targets are: " + targets.stream().map(AldorModuleBuildTarget::getPresentableName).collect(Collectors.toList()));
        return targets;

    }

    @NotNull
    @Override
    public BuildTargetLoader<AldorModuleBuildTarget> createLoader(@NotNull JpsModel model) {
        return AldorBuildTargetTypes.instance.createLoader(this, model);
    }
}
