package aldor.builder;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AldorBuilderService extends BuilderService {
    private static final Logger LOG = Logger.getInstance(AldorBuilderService.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AldorBuildTargetTypes targetTypes;

    @Override
    public String toString() {
        return "{AldorBuildService}";
    }

    public AldorBuilderService() {
        LOG.info("Creating builder service...");
        //noinspection ThisEscapedInObjectConstruction
        targetTypes = new AldorBuildTargetTypes(this);
    }

    /** Returns the list of build target types contributed by this plugin. */
    @Override
    @NotNull
    public List<? extends BuildTargetType<?>> getTargetTypes() {
        return targetTypes.targetTypes();
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
        return targetTypes.createBuilders();
    }

    public ExecutorService executorService() {
        return executorService;
    }

    public AldorBuildTargetTypes targetTypes() {
        return targetTypes;
    }
}
