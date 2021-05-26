package aldor.build.module.editor;

import aldor.build.module.AldorModuleExtension;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

// @deprecated "Need to get module elements loading correctly"
@Deprecated()
public class AldorBuildElementsEditor extends ModuleElementsEditor {
    private static final Logger LOG = Logger.getInstance(AldorBuildElementsEditor.class);
    @Nullable
    private AldorBuildElementsForm form;

    public AldorBuildElementsEditor(ModuleConfigurationState state) {
        super(state);
        this.form = null;
    }

    @Override
    protected JComponent createComponentImpl() {
        Module module = this.getModel().getModule();
        AldorModuleExtension extension = getModel().getModuleExtension(AldorModuleExtension.class);
        AldorModuleExtension uiState = extension.getModifiableModel(true);
        form = new AldorBuildElementsForm(module, uiState, () -> this.fireConfigurationChanged(uiState));
        return form.createComponent();
    }

    private void fireConfigurationChanged(AldorModuleExtension uiState) {
        LOG.info("Changed " + uiState);
    }

    @Override
    public void disposeUIResources() {
        super.disposeUIResources();
    }

    @Override
    public String getDisplayName() {
        return "Aldor Build Settings";
    }

    @Override
    public void canApply() throws ConfigurationException {
        LOG.info("Can apply..");
    }

    @Override
    public boolean isModified() {
        return super.isModified() || ((form != null) && form.isModified());
    }

    @Override
    public void apply() throws ConfigurationException {
        LOG.info("apply.. " + getState());
        if (form != null) {
            form.apply();
        }
    }

    @VisibleForTesting
    public AldorBuildElementsForm form() {
        return form;
    }
}
