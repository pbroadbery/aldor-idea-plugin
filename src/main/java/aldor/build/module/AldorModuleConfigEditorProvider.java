package aldor.build.module;

import aldor.build.facet.aldor.AldorFacetType;
import aldor.builder.jps.AldorSourceRootType;
import com.intellij.ide.JavaUiBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.BuildElementsEditor;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProviderEx;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * UI for editing module config.
 */
public class AldorModuleConfigEditorProvider implements ModuleConfigurationEditorProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorModuleConfigEditorProvider.class);

    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        LOG.info("Creating module config editors for " + state.getProject().getName());
        ModifiableRootModel rootModel = state.getRootModel();
        Module module = rootModel.getModule();
        if (!(ModuleType.get(module) instanceof AldorModuleType)) {
            return ModuleConfigurationEditor.EMPTY;
        }
        List<ModuleConfigurationEditor> editors = new ArrayList<>();
        if (!state.getFacetsProvider().getFacetsByType(module, AldorFacetType.TYPE_ID).isEmpty()) {
            editors.add(new CommonContentEntriesEditor(module.getName(), state, AldorSourceRootType.INSTANCE));
            //editors.add(new AldorBuildElementsEditor(state));
        }

        return editors.isEmpty() ? ModuleConfigurationEditor.EMPTY : editors.toArray(ModuleConfigurationEditor.EMPTY);
    }

    @Override
    public boolean isCompleteEditorSet() {
        return false;
    }

    private static class AldorBuildElementsEditor extends ModuleElementsEditor {
        private JCheckBox compileWithFricasCheckbox = null;
        private FieldPanel outputDirectory = null;
        private FieldPanel testOutputDirectory = null;

        protected AldorBuildElementsEditor(@NotNull ModuleConfigurationState state) {
            super(state);
        }

        @Override
        protected JComponent createComponentImpl() {
            final JPanel outputPathsPanel = new JPanel(new GridBagLayout());


            updatePathsPanel();

            final JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(IdeBorderFactory.createTitledBorder("Aldor Build Directory"));
            panel.add(outputPathsPanel, BorderLayout.NORTH);
            return panel;
        }

        private void updatePathsPanel() {

        }

        @Override
        //@NlsContexts.ConfigurableName
        public String getDisplayName() {
            return null;
        }
    }
}

