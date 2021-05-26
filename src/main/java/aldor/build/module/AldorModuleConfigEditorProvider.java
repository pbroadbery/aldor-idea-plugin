package aldor.build.module;

import aldor.build.facet.aldor.AldorFacetType;
import aldor.build.module.editor.AldorBuildElementsEditor;
import aldor.builder.jps.AldorSourceRootType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProviderEx;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

import java.util.ArrayList;
import java.util.List;

/**
 * UI for editing module config.
 */
public class AldorModuleConfigEditorProvider implements ModuleConfigurationEditorProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorModuleConfigEditorProvider.class);

    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        ModifiableRootModel rootModel = state.getRootModel();
        Module module = rootModel.getModule();
        AldorEnabledModuleExtension aldorEnabled = AldorEnabledModuleExtension.getInstance(module);
        LOG.info("Creating module config editors for " + state.getProject().getName() + " " + aldorEnabled);
        List<ModuleConfigurationEditor> editors = new ArrayList<>();
        if ((aldorEnabled != null) && aldorEnabled.enabled()) {
            editors.add(new CommonContentEntriesEditor(module.getName(), state, AldorSourceRootType.INSTANCE));
            //editors.add(new AldorBuildElementsEditor(state));
        }

        LOG.info("Creating module config editors for " + state.getProject().getName() + " " + editors.size());
        return editors.isEmpty() ? ModuleConfigurationEditor.EMPTY : editors.toArray(ModuleConfigurationEditor.EMPTY);
    }

    @Override
    public boolean isCompleteEditorSet() {
        return false;
    }
}

