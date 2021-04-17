package aldor.runconfiguration.spad;

import aldor.file.SpadInputFileType;
import aldor.sdk.SdkTypes;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;

import static aldor.runconfiguration.spad.SpadInputRunConfigurationType.SpadInputConfigurationBean;

@SuppressWarnings({"serial", "SerializableHasSerializationMethods"})
public class SpadInputConfigurableForm extends JComponent {
    private final Project project;
    private final Sdk sdk;
    private JPanel wholePanel;
    private TextFieldWithBrowseButton myInputFile;
    private JCheckBox loadSpad;
    private JCheckBox keepProcess;
    private JTextPane fricasCommand;

    public SpadInputConfigurableForm(Project project, @Nullable Module module, Sdk sdk) {
        this.project = project;
        this.sdk = sdk;
    }

    public JPanel wholePanel() {
        return wholePanel;
    }

    private void createUIComponents() {
        myInputFile = new TextFieldWithBrowseButton();
        keepProcess = new JCheckBox("");
        fricasCommand = new JTextPane();
        myInputFile.setToolTipText("file name");
        myInputFile.addBrowseFolderListener("Choose Input File", "File to run", project,
                FileChooserDescriptorFactory.createSingleFileDescriptor(SpadInputFileType.INSTANCE));
        myInputFile.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateText();
            }
        });
        keepProcess.addChangeListener(e -> updateText());
    }

    private void updateText() {
        SpadInputConfigurationBean bean = new SpadInputConfigurationBean();
        String execPath = SdkTypes.axiomSysPath(sdk);
        updateConfiguration(bean);
        GeneralCommandLine commandLine = SpadInputProcesses.executionCommandLine(bean, execPath);
        fricasCommand.setText(commandLine.getCommandLineString());
    }

    public void resetEditor(SpadInputConfigurationBean bean) {
        myInputFile.setText(bean.inputFile);
        loadSpad.setSelected(bean.loadSpad);
        keepProcess.setSelected(bean.keepRunning);
        updateText();
    }

    public void updateConfiguration(SpadInputConfigurationBean bean) {
        bean.inputFile = myInputFile.getText();
        bean.loadSpad = loadSpad.isSelected();
        bean.keepRunning = keepProcess.isSelected();
    }
}
