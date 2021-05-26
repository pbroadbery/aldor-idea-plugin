package aldor.build.module;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.serviceContainer.NonInjectable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorEnabledModuleExtension extends ModuleExtension implements PersistentStateComponent<AldorEnabledModuleState> {
    @Nullable
    private final AldorEnabledModuleExtension source;
    private boolean enabled;

    public AldorEnabledModuleExtension() {
        this.enabled = false;
        source = null;
    }

    @NonInjectable
    public AldorEnabledModuleExtension(AldorEnabledModuleExtension source) {
        this.source = source;
        this.enabled = source.enabled();
    }

    public static AldorEnabledModuleExtension getInstance(final Module module) {
        return ModuleRootManager.getInstance(module).getModuleExtension(AldorEnabledModuleExtension.class);
    }

    public boolean enabled() {
        return enabled;
    }

    @Override
    @NotNull
    public AldorEnabledModuleExtension getModifiableModel(boolean writable) {
        return new AldorEnabledModuleExtension(this);
    }

    @Override
    public void commit() {
        if (source != null) {
            source.enabled(this.enabled);
        }
    }

    @SuppressWarnings("BooleanParameter")
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isChanged() {
        if (source == null) {
            return false;
        }
        return this.enabled == this.source.enabled();
    }

    @Override
    public void dispose() {

    }

    @Override
    @Nullable
    public AldorEnabledModuleState getState() {
        return new AldorEnabledModuleState(enabled);
    }

    @Override
    public void loadState(@NotNull AldorEnabledModuleState state) {
        this.enabled = state.aldorEnabled;
    }

    @Override
    public String toString() {
        return "AldorEnabledModuleExtension{" +
                "source=" + source +
                ", enabled=" + enabled +
                '}';
    }
}
