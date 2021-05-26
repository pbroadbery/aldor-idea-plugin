package aldor.build.facet.fricas;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FricasFacetConfiguration implements FacetConfiguration, PersistentStateComponent<FricasFacetProperties> {
    @Nullable
    private FricasFacetProperties state;

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] {new FricasFacetEditorForm(editorContext, validatorsManager)};
    }

    /**
     * Called from the UI
     * @param state - updated state
     */
    void updateState(FricasFacetProperties state) {
        this.state = state;
    }

    @Nullable
    @Override
    public FricasFacetProperties getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull FricasFacetProperties state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        this.state = new FricasFacetProperties(null);
    }

    @Override
    public void initializeComponent() {

    }

    @Nullable
    public Sdk sdk() {
        if (state == null) {
            return null;
        } else {
            if (state.sdkName() == null) {
                return null;
            }
            return ProjectJdkTable.getInstance().findJdk(state.sdkName());
        }
    }
}
