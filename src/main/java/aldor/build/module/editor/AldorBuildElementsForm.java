package aldor.build.module.editor;

import aldor.build.module.AldorModuleExtension;
import aldor.builder.jps.module.AldorModuleState;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;

// @deprecated "Need to get module elements loading correctly"
@Deprecated()
public class AldorBuildElementsForm {
    private static final Logger LOG = Logger.getInstance(AldorBuildElementsForm.class);
    private final Module module;
    private final AldorModuleExtension extension;
    private final Runnable formChanged;

    private JPanel topPanel;
    private JRadioButton generatedMakefileRadioButton;
    private JRadioButton localMakefileRadioButton;
    private JPanel outputDirectoryPanel;
    private FieldPanel outputDirectoryFieldPanel = null;

    public AldorBuildElementsForm(Module module, AldorModuleExtension extension, Runnable formChanged) {
        this.module = module;
        this.extension = extension;
        this.formChanged = formChanged;
    }

    public void createUIComponents() {
        outputDirectoryFieldPanel = createDirectoryFieldPanel(this::commitPanel);
        outputDirectoryPanel = outputDirectoryFieldPanel;

        outputDirectoryFieldPanel.setText(extension.state()._outputDirectory());
    }

    @Nullable
    public JComponent createComponent() {
        return topPanel;
    }

    public boolean isModified() {
        return extension.isChanged();
    }

    public void apply() throws ConfigurationException {
        LOG.info("Apply");
        commitPanel();
        extension.commit();
    }

    FieldPanel createDirectoryFieldPanel(Runnable commit) {
        JTextField field = new ExtendableTextField();
        final FileChooserDescriptor outputPathsChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        outputPathsChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, module);
        outputPathsChooserDescriptor.setHideIgnored(false);
        InsertPathAction.addTo(field, outputPathsChooserDescriptor);
        FileChooserFactory.getInstance().installFileCompletion(field, outputPathsChooserDescriptor, true, null);

        Runnable docListener = () -> {
            LOG.info("Document changed " + outputDirectoryFieldPanel.getText());
        };

        return new FieldPanel(field, null, null,
                new BrowseFilesListener(field, "Title", "Description", outputPathsChooserDescriptor) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LOG.info("Selected File " + field.getText());
                        super.actionPerformed(e);
                    }
                }, docListener);
    }

    private void commitPanel() {
        LOG.info("Commit " + getState());
        extension.setState(getState());
        this.formChanged.run();
    }

    private AldorModuleState getState() {
        return AldorModuleState.newBuilder().build();
    }

    public JPanel panel() {
        return topPanel;
    }
}
