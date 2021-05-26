package aldor.module.template;

import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.util.InstanceCounter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.util.Optional;

public class AldorNewModuleFacetForm {
    private static final Logger LOG = Logger.getInstance(AldorNewModuleFacetForm.class);
    private final Project project;
    private JPanel panel;
    private JCheckBox createMakefilesCheckBox;
    private JTextPane pathHelp;
    private JLabel makefileHelp;
    private ProjectSdksModel model = null;
    private final int id = InstanceCounter.instance().next(AldorNewModuleFacetForm.class);

    AldorNewModuleFacetForm(Project project) {
        this.project = project;
    }

    private Project project() {
        return project;
    }

    public JComponent component() {
        return panel;
    }

    public void createUIComponents() {
        makefileHelp.setEnabled(false);
        pathHelp.setEnabled(false);
    }

    public boolean createMakefiles() {
        return createMakefilesCheckBox.isSelected();
    }
}
