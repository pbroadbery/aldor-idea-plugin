package aldor.build.module;

import aldor.builder.jps.module.AldorModuleState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.serviceContainer.NonInjectable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorModuleExtension extends ModuleExtension implements PersistentStateComponent<AldorModuleState.Wrapper>  {
    @Nullable
    private AldorModuleExtension source;
    private AldorModuleState state = new AldorModuleState();

    public AldorModuleExtension() {
    }

    @NonInjectable
    public AldorModuleExtension(AldorModuleExtension source) {
        this.source = source;
        if (source != null) {
            this.state = new AldorModuleState(source.state);
        }
    }

    @Override
    public String toString() {
        return "AldorModuleExtension{" +
                ", state=" + state +
                "source=" + source +
                '}';
    }

    @NotNull
    @Override
    public AldorModuleExtension getModifiableModel(boolean writable) {
        return new AldorModuleExtension(this);
    }

    public static AldorModuleExtension getInstance(Module module) {
        return ModuleRootManager.getInstance(module).getModuleExtension(AldorModuleExtension.class);
    }

    @Override
    public void commit() {
        if (source != null) {
            source.setState(state);
        }
    }

    public void setState(AldorModuleState state) {
        this.state = state;
    }

    @Override
    public boolean isChanged() {
        if (source == null) {
            return false;
        }
        return !this.state.equals(source.state);
    }

    @Override
    public void dispose() {

    }

    @Override
    @Nullable
    public AldorModuleState.Wrapper getState() {
        return new AldorModuleState.Wrapper(state);
    }

    @Override
    public void loadState(@NotNull AldorModuleState.Wrapper wrapper) {
        this.state = wrapper.state();
    }

    public AldorModuleState state() {
        return state;
    }
}
