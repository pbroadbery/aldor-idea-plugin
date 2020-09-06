package aldor.build.module;

import aldor.builder.jps.AldorSourceRootType;
import com.intellij.openapi.diagnostic.Logger;
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
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * UI for editing module config.
 */
public class AldorModuleConfigEditorProvider implements ModuleConfigurationEditorProviderEx {
    private static final Logger LOG = Logger.getInstance(AldorModuleConfigEditorProvider.class);

    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        LOG.info("Creating module config editors for " + state.getProject().getName());
        ModifiableRootModel rootModel = state.getRootModel();
        Module rootModule = rootModel.getModule();
        if (!(ModuleType.get(rootModule) instanceof AldorModuleType)) {
            return ModuleConfigurationEditor.EMPTY;
        }
        return new ModuleConfigurationEditor[]{
                new CommonContentEntriesEditor(rootModule.getName(), state, JavaSourceRootType.SOURCE, JavaSourceRootType.TEST_SOURCE, AldorSourceRootType.INSTANCE),
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
            outputEditor = new AldorBuildPathEditor(state);
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

    private static class AldorBuildPathEditor extends BuildElementsEditor {

        protected AldorBuildPathEditor(ModuleConfigurationState state) {
            super(state);
        }
    }

    /**
     * Classpath editor tweaked to hide class path boxes (keeps the module setting)
     */
    private static class ModuleJdkEditor extends ClasspathEditor {

        ModuleJdkEditor(ModuleConfigurationState state) {
            super(state);
        }

        @Override
        public JComponent createComponentImpl() {
            JComponent component = super.createComponentImpl();
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                Component childComponent = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (childComponent != null) {
                    childComponent.setVisible(false);
                }
                //noinspection AbsoluteAlignmentInUserInterface
                childComponent = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                if (childComponent != null) {
                    childComponent.setVisible(false);
                }

            }
            return component;
        }
    }

}

