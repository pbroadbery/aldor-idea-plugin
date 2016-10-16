package aldor.build.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.BuildElementsEditor;
import com.intellij.openapi.roots.ui.configuration.ContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProviderEx;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * UI for editing module config.
 */
public class AldorModuleConfigEditor implements ModuleConfigurationEditorProviderEx {
    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        Module module = state.getRootModel().getModule();

        return new ModuleConfigurationEditor[]{new ContentEntriesEditor(module.getName(), state),
                new OutputDirectoryEditor(state)
        };
    }

    @Override
    public boolean isCompleteEditorSet() {
        return true;
    }


    public static class OutputDirectoryEditor extends ModuleElementsEditor {
        private final BuildElementsEditor outputEditor;

        public OutputDirectoryEditor(ModuleConfigurationState state) {
            super(state);
            outputEditor = new AldorBuildElementsEditor(state);
        }

        @Override
        protected JComponent createComponentImpl() {
            return outputEditor.createComponentImpl();
        }

        @Override
        public void saveData() {
            outputEditor.saveData();
        }

        @Nls
        @Override
        public String getDisplayName() {
            return "Aldor Output Directory";
        }

        @Nullable
        @Override
        public String getHelpTopic() {
            return null;
        }
    }

    private static class AldorBuildElementsEditor extends BuildElementsEditor {

        protected AldorBuildElementsEditor(ModuleConfigurationState state) {
            super(state);
        }
    }

}
