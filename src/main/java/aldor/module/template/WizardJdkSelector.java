package aldor.module.template;

import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class WizardJdkSelector extends WizardInputField<JdkComboBox> {
    private static final Logger LOG = Logger.getInstance(WizardJdkSelector.class);
    private final String label;
    @NotNull
    private final JdkComboBox sdkComboSelector;
    private final Set<SdkTypeId> allowedTypes;

    WizardJdkSelector(String id, String label, String defaultValue, Set<SdkTypeId> allowedTypes) {
        super(id, defaultValue);
        this.label = label;
        this.allowedTypes = allowedTypes;
        ProjectSdksModel model = new ProjectSdksModel();
        model.syncSdks();
        this.sdkComboSelector = new JdkComboBox(null, model,
                this::typeFilter, this::sdkFilter, this::creationFilter, WizardJdkSelector::sdkAdded);
    }

    private static void sdkAdded(Sdk sdk) {
        LOG.info("Created sdk " + sdk.getName());
        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
        if (jdkTable.findJdk(sdk.getName()) == null) {
            jdkTable.addJdk(sdk);
            LOG.info("Added new sdk " + sdk.getName());
        }
        else {
            LOG.info("Already known " + sdk.getName());
        }
    }

    protected boolean sdkFilter(Sdk sdk) {
        return true;
    }

    protected boolean typeFilter(SdkTypeId sdkTypeId) {
        return allowedTypes.contains(sdkTypeId);
    }

    protected boolean creationFilter(SdkTypeId sdkTypeId) {
        return typeFilter(sdkTypeId);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public JdkComboBox getComponent() {
        return sdkComboSelector;
    }

    @Override
    public String getValue() {
        return Optional.ofNullable(sdkComboSelector.getSelectedJdk()).map(Sdk::getName).orElse(null);
    }
}
