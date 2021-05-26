package aldor.build.module;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AldorModuleBuildSettings implements PersistentStateComponent<AldorModuleBuildSettings.State> {
    private State state = new State();
//  NB:             outputDirectoryFieldPanel.setText(Optional.ofNullable(ProjectUtil.guessModuleDir(module())).map(x -> x.getPath() + "/out").orElse(""));
    @Override
    @Nullable
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        // Called when the component is initialised, but no state loaded
        PersistentStateComponent.super.noStateLoaded();
    }

    public static class State {
        private AldorMakeDirectoryOption makeDirectoryOption = AldorMakeDirectoryOption.Source;
        @Nullable private String buildPath = null;
    }
}
