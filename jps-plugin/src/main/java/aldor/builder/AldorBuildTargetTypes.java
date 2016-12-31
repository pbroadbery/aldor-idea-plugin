package aldor.builder;

import aldor.builder.files.AldorFileBuildTargetType;
import aldor.builder.files.AldorFileTargetBuilder;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.model.JpsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store of target types and mapping of target types to builder.
 */
@SuppressWarnings("PublicField")
public class AldorBuildTargetTypes {
    private static final Logger LOG = Logger.getInstance(AldorBuildTargetTypes.class);

    public final AldorFileBuildTargetType fileBuildTargetType;

    AldorBuildTargetTypes(AldorBuilderService service) {
        LOG.info("Builder: " + service);
        this.fileBuildTargetType = new AldorFileBuildTargetType(service);
    }

    public static <Target extends BuildTarget<?>> BuildTargetLoader<Target> createLoader(@NotNull final BuildTargetType<Target> type, @NotNull final JpsModel model) {
        final Map<String, Target> targetMap = new HashMap<>();

        for (Target target : type.computeAllTargets(model)) {
            targetMap.put(target.getId(), target);
        }
        return new BuildTargetLoader<Target>() {
            @Nullable
            @Override
            public Target createTarget(@NotNull String targetId) {
                LOG.info("Creating loader for: " + targetId);
                Target target = targetMap.get(targetId);
                if (target == null) {
                    LOG.info("Target id: "+ targetId + " " + targetMap.keySet().stream().findFirst());
                }
                return target;
            }
        };
    }


    public List<BuildTargetType<? extends BuildTarget<?>>> targetTypes() {
        return Lists.newArrayList(fileBuildTargetType);
    }

    public List<? extends TargetBuilder<?, ?>> createBuilders() {
        List<TargetBuilder<?, ?>> list = new ArrayList<>();
        list.add(new AldorFileTargetBuilder(fileBuildTargetType));
        return list;
    }

}
