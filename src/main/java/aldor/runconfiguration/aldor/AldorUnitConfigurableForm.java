package aldor.runconfiguration.aldor;

import com.intellij.openapi.project.Project;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AldorUnitConfigurableForm {
    private JPanel wholePanel;
    private JTextField unitProperty;
    private JTextField fileName;
    private JTextField typeName;

    public AldorUnitConfigurableForm(Project project) {

    }

    public void resetEditor(AldorUnitConfiguration configuration) {
        fileName.setText(configuration.bean().inputFile);
        typeName.setText(configuration.bean().typeName);
    }

    public void updateConfiguration(AldorUnitConfiguration configuration) {
        configuration.bean().typeName = typeName.getText();
        configuration.bean().inputFile = fileName.getText();
    }

    public JComponent wholePanel() {
        return wholePanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
