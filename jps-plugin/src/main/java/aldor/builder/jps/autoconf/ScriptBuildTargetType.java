package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.BuildInstanceModel;
import aldor.builder.jps.autoconf.descriptors.InstantiableBuildTargetType;
import aldor.builder.jps.autoconf.descriptors.ScriptTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.ScriptType;
import aldor.builder.jps.util.Sx;
import aldor.util.SxForm;
import aldor.util.TargetTypeIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public class ScriptBuildTargetType
        extends Sx.BuildTargetType<ScriptBuildTarget>
        implements InstantiableBuildTargetType<SimpleSourceRoot, ScriptTargetDescriptor, ScriptBuildTarget> {
    private static final Logger LOG = Logger.getInstance(ScriptBuildTargetType.class);
    public static final String ID = "Script";
    public static final TargetTypeIdentifier<ScriptBuildTargetType> ident = new TargetTypeIdentifier<>(ScriptBuildTargetType.class, ID);
    private final BuildStaticState staticState;
    private final TargetFactory targetFactory;

    public ScriptBuildTargetType(BuildStaticState staticState, TargetFactory targetFactory) {
        super(ID, true);
        if (staticState == null) {
            throw new IllegalStateException("Expected static state to be supplied");
        }
        this.staticState = staticState;
        this.targetFactory = targetFactory;
    }

    @Override
    @NotNull
    public SxForm sxForm() {
        return list().add(name("script-build-target-type"));
    }

    @Override
    @NotNull
    public List<ScriptBuildTarget> computeAllTargets(@NotNull JpsModel jpsModel) {
        Collection<BuildInstanceModel> instanceModels = staticState.updateJpsModel(jpsModel);
        LOG.info("Compute all: Script " + jpsModel.getProject());

        if (instanceModels == null) {
            throw new IllegalStateException();
        }
        var targetDescriptors = instanceModels.stream()
                .peek(model -> LOG.info("Targets for " + model.sxForm().asSExpression()))
                .flatMap(instanceModel -> instanceModel.allScriptTargets().stream().map(x -> Pair.of(instanceModel, x)))
                .toList();
        LOG.info("Computed targets: " + targetDescriptors.size() + " " + this.sxForm().asSExpression());
        for (var targetPair: targetDescriptors.stream().limit(10).toList()) {
            LOG.info("Target: " + targetPair.getRight().sxForm().asSExpression());
        }

        return targetDescriptors.stream()
                .map(pair -> new ScriptBuildTarget(this, pair.getLeft().jpsModule(), pair.getRight()))
                .toList();
    }

    @Override
    public @NotNull BuildTargetLoader<ScriptBuildTarget> createLoader(@NotNull JpsModel jpsModel) {
        LOG.info("Create loader..");
        Map<String, ScriptBuildTarget> mapping = computeAllTargets(jpsModel).stream()
                .collect(Collectors.toMap(tgt -> tgt.getId(), tgt -> tgt));
        return new BuildTargetLoader<>() {
            @Override
            @Nullable
            public ScriptBuildTarget createTarget(@NotNull String targetId) {
                return mapping.get(targetId);
            }
        };
    }

    public String encode(JpsModule module, ScriptTargetDescriptor descriptor) {
        ScriptType.Kind kind = descriptor.scriptType().kind();
        if (kind == ScriptType.Kind.Make) {
            return String.format("{%s}-{%s}-{%s}", module.getName(), (descriptor.scriptType().subdirectory() == null) ? kind.name() : descriptor.scriptType().subdirectory(), descriptor.scriptType().targetName());
        }
        else {
            return String.format("{%s}-{%s}", module.getName(), kind.name());
        }
    }


    @Override
    public ScriptBuildTarget newTarget(JpsModule module, ScriptTargetDescriptor descriptor) {
        return new ScriptBuildTarget(this, module, descriptor);
    }

    public TargetFactory targetFactory() {
        return targetFactory;
    }
}
