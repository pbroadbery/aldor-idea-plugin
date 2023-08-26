package aldor.builder;

import aldor.builder.files.AldorFileBuildTargetType;
import aldor.builder.files.AldorFileTargetBuilder;
import aldor.builder.jars.AldorJarBuildTargetType;
import aldor.builder.jars.AldorJarTargetBuilder;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.model.JpsModel;

import java.util.ArrayList;
import java.util.Arrays;
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
    public final AldorJarBuildTargetType jarBuildTargetType;

    @Nullable
    private List<TargetBuilder<?, ?>> builders;

    public AldorBuildTargetTypes(AldorBuilderService service) {
        LOG.info("Builder: " + service);
        this.fileBuildTargetType = new AldorFileBuildTargetType(service);
        this.jarBuildTargetType = new AldorJarBuildTargetType(service);
        this.builders = null;
    }

    public static <Target extends BuildTarget<?>> BuildTargetLoader<Target> createLoader(@NotNull final BuildTargetType<Target> type,
                                                                                         @NotNull final JpsModel model) {
        final Map<String, Target> targetMap = new HashMap<>();

        for (Target target : type.computeAllTargets(model)) {
            targetMap.put(target.getId(), target);
        }
        return new BuildTargetLoader<Target>() {
            @Nullable
            @Override
            public Target createTarget(@NotNull String targetId) {
                LOG.info("Creating loader for: " + type + ": " + targetId);
                Target target = targetMap.get(targetId);
                if (target == null) {
                    LOG.info("Target id: "+ targetId + " not in original set.. " + targetMap.keySet().stream().findFirst());
                }
                return target;
            }
        };
    }


    public List<BuildTargetType<? extends BuildTarget<?>>> targetTypes() {
        return Arrays.asList(jarBuildTargetType, fileBuildTargetType);
    }

    public List<? extends TargetBuilder<?, ?>> builders() {
        if (builders == null) {
            List<TargetBuilder<?, ?>> list = new ArrayList<>();
            list.add(new AldorFileTargetBuilder(fileBuildTargetType));
            list.add(new AldorJarTargetBuilder(jarBuildTargetType));
            //list.addAll(buildStaticModel.targetBuilders());
            //list.add(new MakeTargetBuilder(makeBuildTargetType));
            //list.add(new AutoconfTargetBuilder(autoconfTargetType));
            builders = list;
        }
        return builders;
    }

}
