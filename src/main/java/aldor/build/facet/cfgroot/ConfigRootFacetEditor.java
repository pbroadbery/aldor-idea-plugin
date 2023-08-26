package aldor.build.facet.cfgroot;

import aldor.build.facet.FacetPropertiesEditorTab;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class ConfigRootFacetEditor extends FacetPropertiesEditorTab<ConfigRootFacetProperties, ConfigRootFacetConfiguration> {
    private static final Logger LOG = Logger.getInstance(ConfigRootFacetEditor.class);
    private final ConfigRootFacetConfiguration configuration;
    private ConfigRootConfigurable form = null;

    protected ConfigRootFacetEditor(ConfigRootFacetConfiguration facetConfiguration, FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        super(editorContext, validatorsManager);
        this.configuration = facetConfiguration;
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new ConfigRootConfigurable(this.facetState());
        }
        return form.createComponent();
    }

    @Override
    public void onFacetInitialized(@NotNull Facet facet) {
        LOG.info("Initialise: " + facet.getConfiguration());
        form.reset();
    }

    @Override
    public boolean isModified() {
        return form.isModified();
    }

    @Override
    public void apply() {
        LOG.info("Apply - Current: " + facet().getConfiguration().getState());
        LOG.info("Apply - New: " + form.currentState());
        form.apply();
        configuration.updateState(form.currentState());
    }

    @Override
    public void reset() {
        form.reset();
    }

    @Override
    public String getDisplayName() {
        return "Root Properties Editor";
    }

    @Override
    @NotNull
    public ConfigRootFacetProperties currentState() {
        return form.currentState();
    }

}