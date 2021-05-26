package aldor.module.template.wizard;

import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WizardJdkSelector extends WizardInputField<JdkComboBox> {
    private static final Logger LOG = Logger.getInstance(WizardJdkSelector.class);
    private final String label;
    @NotNull
    private final JdkComboBox sdkComboSelector;
    private final Set<SdkTypeId> allowedTypes;
    private final ProjectSdksModel model;
    @Nullable
    private String valueOverride;

    public WizardJdkSelector(String id, String label, String defaultValue, Set<SdkTypeId> allowedTypes) {
        super(id, defaultValue);
        this.label = label;
        this.allowedTypes = allowedTypes;
        model = new ProjectSdksModel();
        LOG.info("Current SDKs: " + Arrays.stream(model.getSdks()).map(Sdk::getName).collect(Collectors.toList()));
        model.syncSdks();
        LOG.info("After Sync: Current SDKs: " + Arrays.stream(model.getSdks()).map(Sdk::getName).collect(Collectors.toList()));
        this.sdkComboSelector = new JdkComboBox(null, model,
                this::typeFilter, this::sdkFilter, this::creationFilter, this::sdkAdded);
        this.valueOverride = null;
    }

    private void sdkAdded(Sdk sdk) {
        if ((sdk == null) || (model == null)) {
            return;
        }
        LOG.info("Adding SDK: " + sdk.getName());
        if (model.findSdk(sdk.getName()) != null) {
            LOG.info("SDK " + sdk.getName() + " already exists");
        }
        LOG.info("Current SDKs: " + Arrays.stream(model.getSdks()).map(x -> x.getName()).collect(Collectors.toList()));
        try {
            model.apply(null, true);
            LOG.info("Applied: Current SDKs: " + Arrays.stream(model.getSdks()).map(x -> x.getName()).collect(Collectors.toList()));
            LOG.info("All sdks: " + Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).map(x -> x.getName()).collect(Collectors.toList()));
        }
        catch (ConfigurationException e) {
            LOG.error("while creating SDK " + sdk.getName(), e);
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
        if (valueOverride != null) {
            return valueOverride;
        }
        return Optional.ofNullable(sdkComboSelector.getSelectedJdk()).map(Sdk::getName).orElse(null);
    }

    @Override
    @TestOnly
    public void setValue(String value) {
        this.valueOverride = value;
    }
}
