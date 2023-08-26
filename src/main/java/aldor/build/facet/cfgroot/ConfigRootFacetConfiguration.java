package aldor.build.facet.cfgroot;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigRootFacetConfiguration implements FacetConfiguration, PersistentStateComponent<ConfigRootFacetProperties> {
    private static final Logger LOG = Logger.getInstance(ConfigRootFacetConfiguration.class);
    private static final FacetEditorTab[] NO_EDITOR_TABS = new FacetEditorTab[0];
    private ConfigRootFacetProperties state = ConfigRootFacetProperties.newBuilder().build();

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new ConfigRootFacetEditor(this, editorContext, validatorsManager)};
    }

    @Override
    public @Nullable ConfigRootFacetProperties getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull ConfigRootFacetProperties state) {
        this.state = state.asBuilder().build();
    }

    public void updateState(ConfigRootFacetProperties currentState) {
        this.state = currentState.copy();
    }
}
