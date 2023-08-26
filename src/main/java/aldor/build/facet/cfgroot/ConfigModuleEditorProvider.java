package aldor.build.facet.cfgroot;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProviderEx;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.ui.VerticalFlowLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public class ConfigModuleEditorProvider implements ModuleConfigurationEditorProviderEx {
    @Override
    public boolean isCompleteEditorSet() {
        return false;
    }

    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        Module module = state.getCurrentRootModel().getModule();
        @Nullable ConfigRootFacet facet = ConfigRootFacet.forModule(module);
        List<ModuleConfigurationEditor> editors = new ArrayList<>();
        if (facet != null) {
            editors.add(new CommonContentEntriesEditor(module.getName(), state));
            // Would be nice to have, but it's hard to modify facets as part of a module editor.
            // A better approach might be an 'enabled' flag on the facet, then have some module-local settings
            // that could be edited in line.
            //editors.add(new ConfigRootBuildLocationEditor(module.getName(), state));
        }
        return editors.toArray(ModuleConfigurationEditor[]::new);
    }

    static class ConfigContentEntriesEditor extends CommonContentEntriesEditor {

        public ConfigContentEntriesEditor(String moduleName, ModuleConfigurationState state) {
            super(moduleName, state);
        }

        @Override
        protected @Nullable JPanel createBottomControl(Module module) {
            JPanel panel = new JPanel(new VerticalFlowLayout());
            panel.add(new JLabel("Just a test.."));
            return super.createBottomControl(module);
        }
    }

}
