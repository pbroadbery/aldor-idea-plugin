package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.Collections;
import java.util.List;

public class AldorBuilderService extends BuilderService {
    private static final Logger LOG = Logger.getInstance(AldorBuilderService.class);

    public AldorBuilderService() {
    }

    /** Returns the list of build target types contributed by this plugin. */
    @Override
    @NotNull
    public List<? extends BuildTargetType<?>> getTargetTypes() {
        return AldorBuildTargetTypes.instance.targetTypes();
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
        return AldorBuildTargetTypes.instance.createBuilders();
    }
}
