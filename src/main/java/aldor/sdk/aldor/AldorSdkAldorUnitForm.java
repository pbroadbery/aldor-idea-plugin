package aldor.sdk.aldor;

import aldor.sdk.NamedSdk;
import aldor.sdk.aldorunit.AldorUnitSdkType;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AldorSdkAldorUnitForm {
    private JCheckBox enableAldorUnit;
    private JdkComboBox jdkComboBox;
    private JButton editButton;
    private JButton newButton;
    private JPanel wholePanel;
    private JTextField javaClassDirectory;

    public void createUIComponents() {
        ProjectSdksModel sdksModel = new ProjectSdksModel();
        sdksModel.reset(null);
        this.jdkComboBox = new JdkComboBox(sdksModel, id -> AldorUnitSdkType.instance().equals(id));
        this.newButton = new JButton("...");
        this.jdkComboBox.setSetupButton(newButton, null, sdksModel, null,
                sdk -> AldorUnitSdkType.instance().equals(sdk.getSdkType()), false);
    }

    public JComponent wholePanel() {
        return wholePanel;
    }

    public void saveSettings(AldorSdkAdditionalData bean) {
        bean.aldorUnitEnabled = this.enableAldorUnit.isSelected();
        JdkComboBox.JdkComboBoxItem selectedItem = jdkComboBox.getSelectedItem();
        if (selectedItem != null) {
            bean.aldorUnitSdk = NamedSdk.namedSdk(jdkComboBox.getSelectedItem());
        }
    }

    public void reset(AldorSdkAdditionalData bean) {
        this.enableAldorUnit.setSelected(bean.aldorUnitEnabled);
        NamedSdk.initialiseJdkComboBox(bean.aldorUnitSdk, this.jdkComboBox);
    }
}
