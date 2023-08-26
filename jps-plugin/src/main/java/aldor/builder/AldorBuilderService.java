package aldor.builder;

import aldor.builder.jps.autoconf.BuildStaticState;
import aldor.builder.jps.autoconf.NoOpTargetBuilder;
import aldor.builder.jps.autoconf.PhonyTargets;
import aldor.builder.jps.autoconf.ScriptBuildTargetType;
import aldor.builder.jps.autoconf.ScriptTargetBuilder;
import aldor.builder.jps.autoconf.StaticExecutionEnvironment;
import aldor.builder.jps.autoconf.TargetFactory;
import aldor.builder.jps.autoconf.descriptors.BuildStaticModel;
import aldor.builder.jps.autoconf.descriptors.BuildStaticModelImpl;
import aldor.util.HasSxForm;
import aldor.util.InstanceCounter;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static aldor.util.SxFormUtils.name;
import static aldor.util.SxFormUtils.number;
import static aldor.util.SxFormUtils.tagged;

public class AldorBuilderService extends BuilderService implements HasSxForm {
    private static final Logger LOG = Logger.getInstance(AldorBuilderService.class);
    private final int instanceId = InstanceCounter.instance().next(AldorBuilderService.class);
    private final AldorBuildTargetTypes targetTypes;
    private final ScriptBuildTargetType scriptTargetType;
    private final PhonyTargets.PhonyTargetType phonyTargetType;
    private TargetBuilder<?, ?> scriptTargetBuilder;
    private TargetBuilder<?, ?> phonyBuilder;

    @Override
    public String toString() {
        return "{AldorBuilderService-" + instanceId +"}";
    }

    public AldorBuilderService() {
        LOG.info("Creating builder service...");
        // FIXME: Be less circular..
        //noinspection ThisEscapedInObjectConstruction
        targetTypes = new AldorBuildTargetTypes(this);
        StaticExecutionEnvironment env = new StaticExecutionEnvironment();
        BuildStaticModel staticModel = new BuildStaticModelImpl(env);
        BuildStaticState.instance().setStaticState(staticModel, env);
        TargetFactory targetFactory = new TargetFactory();
        BuildStaticState staticState = BuildStaticState.instance();
        this.scriptTargetType = new ScriptBuildTargetType(staticState, targetFactory);
        this.phonyTargetType = new PhonyTargets.PhonyTargetType(staticState, targetFactory);
        this.scriptTargetBuilder = new ScriptTargetBuilder(staticState.executionEnvironment(), scriptTargetType);
        this.phonyBuilder = new NoOpTargetBuilder(List.of(phonyTargetType));
    }


    /** Returns the list of build target types contributed by this plugin. */
    @Override
    @NotNull
    public List<? extends BuildTargetType<?>> getTargetTypes() {
        BuildStaticState staticState = BuildStaticState.instance();
        List<BuildTargetType<?>> types = new ArrayList<>();
        types.addAll(targetTypes.targetTypes());
        types.add(scriptTargetType);
        types.add(phonyTargetType);
        return types;
    }

    /**
     * Returns the list of Java module builder extensions contributed by this plugin.
     */
    @Override
    @NotNull
    public List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
        return Collections.emptyList();
    }

    /**
     * Returns the list of non-module target builders contributed by this plugin.
     */
    @Override
    @NotNull
    public List<? extends TargetBuilder<?,?>> createBuilders() {
        LOG.info("Creating builders for aldor build service...");
        List<TargetBuilder<?,?>> builders = new ArrayList<>();
        builders.addAll(targetTypes.builders());
        builders.add(scriptTargetBuilder);
        builders.add(phonyBuilder);
        return builders;
    }

    public AldorBuildTargetTypes targetTypes() {
        return targetTypes;
    }

    @Override
    public @NotNull SxForm sxForm() {
        SxForm properties = tagged()
                .with("instanceId", number(instanceId))
                .with("targets", getTargetTypes().stream().map(tt -> SxFormUtils.asForm(tt)).collect(SxFormUtils.collectList()))
                .with("builders", targetTypes.builders().stream().map(tt -> SxFormUtils.asForm(tt)).collect(SxFormUtils.collectList()));
        return SxFormUtils.list()
                .add(name("AldorBuilderService"))
                .add(properties);
    }
}
