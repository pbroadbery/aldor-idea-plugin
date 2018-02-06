package aldor.spad.runconfiguration;

import aldor.file.SpadInputFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import static aldor.spad.runconfiguration.SpadInputRunConfigurationType.SpadInputConfigurationBean;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class SpadInputConfigurableForm extends JComponent {
    private final Project project;
    private JPanel wholePanel;
    private TextFieldWithBrowseButton myInputFile;
    private JCheckBox loadSpad;
    private JCheckBox keepProcess;

    public SpadInputConfigurableForm(Project project) {
        this.project = project;
    }

    public JPanel wholePanel() {
        return wholePanel;
    }

    private void createUIComponents() {
        myInputFile = new TextFieldWithBrowseButton();
        myInputFile.setToolTipText("file name");
        myInputFile.addBrowseFolderListener("Choose Input File", "File to run", project,
                FileChooserDescriptorFactory.createSingleFileDescriptor(SpadInputFileType.INSTANCE));
    }

    public void resetEditor(SpadInputConfigurationBean bean) {
        myInputFile.setText(bean.inputFile);
        loadSpad.setSelected(bean.loadSpad);
        keepProcess.setSelected(bean.keep);
    }

    public void updateConfiguration(SpadInputConfigurationBean bean) {
        bean.inputFile = myInputFile.getText();
        bean.loadSpad = loadSpad.isSelected();
        bean.keep = keepProcess.isSelected();
    }
}
