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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Optional;

public class AldorNewModuleFacetForm {
    private static final Logger LOG = Logger.getInstance(AldorNewModuleFacetForm.class);
    private final Project project;
    private JdkComboBox aldorSdkComboBox;
    private JTextField buildDirectoryTextField;
    private JPanel panel;
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
        LOG.info("Creating components for " + null);
        model = new ProjectSdksModel();
        Condition<SdkTypeId> aldorSdkFilter = sdkType -> AldorInstalledSdkType.instance().equals(sdkType);
        aldorSdkComboBox = new JdkComboBox(project(), model, aldorSdkFilter, null, aldorSdkFilter, this::sdkAdded);
    }

    private void sdkAdded(Sdk sdk) {
        if ((sdk == null) || (model == null)) {
            return;
        }
        LOG.info("Adding SDK: " + sdk.getName());
        model.addSdk(sdk);
        try {
            model.apply(null, true);
        }
        catch (ConfigurationException e) {
            LOG.error("while creating SDK " + sdk.getName(), e);
        }
    }

    public String aldorSdkName() {
        return Optional.ofNullable(aldorSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null);
    }
}
