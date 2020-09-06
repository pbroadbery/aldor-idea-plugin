package aldor.build.module;


import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker;
import com.intellij.openapi.components.State;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is the wrong way to do module properties - can be embedded in the module xml // FIXME
 */
@Deprecated
@State(
        name = "AldorModuleBuildPath",
        reportStatistic = true/*,
        storages = {
                @Storage(StoragePathMacros.MODULE_FILE)
        }*/)
public class AldorModulePathService implements PersistentStateComponentWithModificationTracker<AldorModuleState> {
    private static final Logger LOG = Logger.getInstance(AldorModulePathService.class);
    private AldorModuleState state;

    public AldorModulePathService() {
        LOG.info("Creating module path service");
        this.state = new AldorModuleState("out/ao", AldorMakeDirectoryOption.Source, true);
    }

    public static AldorModulePathService getInstance(final Module module) {
        return ModuleServiceManager.getService(module, AldorModulePathService.class);
    }

    @Override
    public long getStateModificationCount() {
        return state.getModificationCount();
    }

    @Nullable
    @Override
    public AldorModuleState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull AldorModuleState state) {
        this.state = state;
    }
}
