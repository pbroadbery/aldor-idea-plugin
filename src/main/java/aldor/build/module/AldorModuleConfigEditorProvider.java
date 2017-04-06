package aldor.build.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.BuildElementsEditor;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProviderEx;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * UI for editing module config.
 */
public class AldorModuleConfigEditorProvider implements ModuleConfigurationEditorProviderEx {
    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        ModifiableRootModel rootModel = state.getRootModel();
        Module rootModule = rootModel.getModule();
        if (!(ModuleType.get(rootModule) instanceof AldorModuleType)) {
            return ModuleConfigurationEditor.EMPTY;
        }
        return new ModuleConfigurationEditor[]{
                new CommonContentEntriesEditor(rootModule.getName(), state),
                new OutputDirectoryEditor(state),
                new ClasspathEditor(state)
        };
    }

    @Override
    public boolean isCompleteEditorSet() {
        return false;
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

