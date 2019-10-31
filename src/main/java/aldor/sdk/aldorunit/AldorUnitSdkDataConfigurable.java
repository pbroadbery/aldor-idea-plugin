package aldor.sdk.aldorunit;

import aldor.sdk.NamedSdk;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AldorUnitSdkDataConfigurable implements AdditionalDataConfigurable {
    private final JLabel myParameterLabel = new JLabel("Do the thing");
    private final JCheckBox myParameterCheckbox = new JCheckBox();
    private final JdkComboBox jdkComboBox;
    private final AldorUnitAdditionalData bean = new AldorUnitAdditionalData(); // current state
    private final AldorUnitAdditionalData given = new AldorUnitAdditionalData();
    private Sdk currentSdk = null;

    AldorUnitSdkDataConfigurable() {
        ProjectSdksModel sdksModel = new ProjectSdksModel();
        sdksModel.reset(null);
        jdkComboBox = new JdkComboBox(sdksModel, id -> id instanceof JavaSdkType);
    }

    @Override
    public void setSdk(Sdk sdk) {
        if ((sdk != null) && !sdk.getSdkType().equals(AldorUnitSdkType.instance())) {
            throw new IllegalStateException("Expecting a AldorUnit SDK");
        }
        if (sdk == null) {
            return;
        }
        currentSdk = sdk;
        AldorUnitAdditionalData additional = (AldorUnitAdditionalData) sdk.getSdkAdditionalData();
        if (additional != null) {
            additional = (AldorUnitAdditionalData) sdk.getSdkAdditionalData();
            additional.copyInto(given);
            additional.copyInto(bean);
        }
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        NamedSdk jdk = (bean.jdk.name() != null) ? bean.jdk : new NamedSdk("<Unknown>");
        NamedSdk.initialiseJdkComboBox(jdk, jdkComboBox);
        jdkComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sdk selectedJdk = jdkComboBox.getSelectedJdk();
                onSdkSelected(selectedJdk);
            }
        });
        JPanel wholePanel = new JPanel(new GridBagLayout());
        wholePanel.add(myParameterLabel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));
        wholePanel.add(myParameterCheckbox, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));
        wholePanel.add(jdkComboBox, new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));

        return wholePanel;
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void onSdkSelected(Sdk jdk) {
        bean.jdk = new NamedSdk(jdk);
    }

    @Override
    public boolean isModified() {
        return !bean.matches(given);
    }

    @Override
    public void apply() {
        if (currentSdk != null) {
            if (currentSdk.getSdkAdditionalData() == null) {
                SdkModificator sdkModificator = currentSdk.getSdkModificator();
                sdkModificator.setSdkAdditionalData(given);
                sdkModificator.commitChanges();
            }
            bean.copyInto((AldorUnitAdditionalData) currentSdk.getSdkAdditionalData());
        }
    }

}