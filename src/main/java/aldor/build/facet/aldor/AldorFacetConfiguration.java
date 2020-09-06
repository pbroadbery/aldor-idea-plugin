package aldor.build.facet.aldor;

import aldor.build.facet.SpadFacet;
import aldor.builder.jps.AldorModuleExtensionProperties;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorFacetConfiguration implements FacetConfiguration, PersistentStateComponent<AldorModuleExtensionProperties>, SpadFacet<AldorModuleExtensionProperties> {
    private static final Logger LOG = Logger.getInstance(AldorFacetConfiguration.class);
    public static final FacetEditorTab[] NO_EDITOR_TABS = new FacetEditorTab[0];
    private AldorModuleExtensionProperties state = new AldorModuleExtensionProperties();

    public AldorFacetConfiguration() {}

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] {new AldorFacetEditorForm(editorContext, validatorsManager)}; // FIXME: BuildPath/SDK/Java
    }

    @Nullable
    @Override
    public AldorModuleExtensionProperties getState() {
        return state;
    }

    /**
     * Called from the UI
     * @param state - updated state
     */
    void updateState(AldorModuleExtensionProperties state) {
        this.state = state;
    }

    @Override
    public void loadState(@NotNull AldorModuleExtensionProperties state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        LOG.info("No state loaded");
    }

    @Override
    public void initializeComponent() {
        LOG.info("Initialise component");
    }

    @Nullable
    public Sdk sdk() {
        if ((state == null) || (state.sdkName() == null)) {
            return null;
        }

        return ProjectJdkTable.getInstance().findJdk(state.sdkName());
    }

    @Override
    public Optional<AldorModuleExtensionProperties> getProperties() {
        //noinspection CallToSimpleGetterFromWithinClass
        return Optional.ofNullable(getState());
    }
}
