package aldor.builder;

import aldor.builder.files.AldorFileBuildTargetType;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.model.JpsModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store of target types and mapping of target types to builder.
 */
@SuppressWarnings("PublicField")
public class AldorBuildTargetTypes {
    private static final Logger LOG = Logger.getInstance(AldorBuildTargetTypes.class);

    public static final AldorBuildTargetTypes instance = new AldorBuildTargetTypes();

    public final AldorSourceRootBuildTargetType sourceRootTargetType = new AldorSourceRootBuildTargetType();
    public final AldorModuleBuildTargetType moduleBuildTargetType = new AldorModuleBuildTargetType();
    public final AldorFileBuildTargetType fileBuildTargetType = new AldorFileBuildTargetType();

    public <Target extends BuildTarget<?>> BuildTargetLoader<Target> createLoader(@NotNull final BuildTargetType<Target> type, @NotNull final JpsModel model) {
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
                    System.out.println("Target id: "+ targetId + " " + targetMap.keySet().stream().findFirst());
                }
                return target;
            }
        };
    }


    public List<BuildTargetType<? extends BuildTarget<?>>> targetTypes() {
        return Lists.newArrayList(sourceRootTargetType, moduleBuildTargetType, fileBuildTargetType);
    }

    public List<? extends TargetBuilder<?, ?>> createBuilders() {
        List<TargetBuilder<?, ?>> list = new ArrayList<>();
        list.add(new AldorBuilder(sourceRootTargetType));
        list.add(new EmptyBuilder(Collections.singletonList(moduleBuildTargetType)));
        list.add(new AldorFileBuildTargetType.AldorFileTargetBuilder(fileBuildTargetType));
        return list;
    }


    private class EmptyBuilder extends TargetBuilder<AldorRootDescriptor, AldorModuleBuildTarget> {

        protected EmptyBuilder(Collection<? extends BuildTargetType<? extends AldorModuleBuildTarget>> buildTargetTypes) {
            super(buildTargetTypes);
        }

        @Override
        public void build(@NotNull AldorModuleBuildTarget target,
                          @NotNull DirtyFilesHolder<AldorRootDescriptor, AldorModuleBuildTarget> holder, @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException, IOException {
            LOG.info("Building module: " + target);
        }

        @NotNull
        @Override
        public String getPresentableName() {
            return "Aldor-Empty-Builder";
        }
    }

}
