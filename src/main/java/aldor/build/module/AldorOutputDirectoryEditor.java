package aldor.build.module;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

/*
 * Would be nice to have a custom editor
 */
class AldorOutputDirectoryEditor extends ModuleElementsEditor {
    private static final Logger LOG = Logger.getInstance(AldorOutputDirectoryEditor.class);

    AldorBuildLocationForm locationForm = new AldorBuildLocationForm();

    protected AldorOutputDirectoryEditor(@NotNull ModuleConfigurationState state) {
        super(state);
    }

    @Override
    protected JComponent createComponentImpl() {
        return locationForm;
    }


    @Nls
    @Override
    public String getDisplayName() {
        return "Local Build Location";
    }

    @NotNull
    @Override
    protected ModuleConfigurationState getState() {
        return super.getState();
    }

    @Override
    public void saveData() {
        String directory = locationForm.buildDirectory();
        AldorModuleExtension extension = AldorModuleExtension.getInstance(getState().getRootModel().getModule());
        extension.getState().setOutputDirectory(directory);
        fireConfigurationChanged();
    }
}
