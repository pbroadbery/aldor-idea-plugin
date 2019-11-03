package aldor.build.module;

import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker;
import com.intellij.openapi.components.State;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@State(
        name = "AldorModuleBuildPath",
        reportStatistic = true
        /*storages = {
                @Storage(StoragePathMacros.MODULE_FILE)
        }*/
)
@Deprecated
public class AldorModuleExtension extends ModuleExtension implements
        PersistentStateComponentWithModificationTracker<AldorModuleState> {

    private static final Logger LOG = Logger.getInstance(AldorModuleExtension.class);

    @Nullable
    private Module module;
    private boolean writable;
    private AldorModuleExtension source;

    @Nullable
    private AldorModuleState state = new AldorModuleState("out/ao", AldorMakeDirectoryOption.Source);

    @Override
    public long getStateModificationCount() {
        return (state == null) ? Long.MAX_VALUE : state.getModificationCount();
    }

    public static AldorModuleExtension getInstance(final Module module) {
        return ModuleRootManager.getInstance(module).getModuleExtension(AldorModuleExtension.class);
    }

    public AldorModuleExtension(Module module) {
        this(module, null, false);
    }

    public AldorModuleExtension(@NotNull Module module, AldorModuleExtension source, boolean isWritable) {
        this.module = module;
        this.source = source;
        this.writable = isWritable;
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

    @Override
    public AldorModuleExtension getModifiableModel(final boolean writable) {
        assert module != null;
        return new AldorModuleExtension(module, this, writable);
    }

    @Override
    public void commit() {
        LOG.info("Saving state (not implemented)");
    }

    @Override
    public boolean isChanged() {
        assert state != null;
        //noinspection AccessingNonPublicFieldOfAnotherObject
        return (source != null) && !Objects.equals(source.state, state);
    }

    @Override
    public void dispose() {
        module = null;
        state = null;
    }

}
