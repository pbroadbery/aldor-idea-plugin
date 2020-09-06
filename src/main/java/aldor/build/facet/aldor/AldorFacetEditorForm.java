package aldor.build.facet.aldor;

import aldor.build.facet.FacetPropertiesEditorTab;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.builder.jps.JpsAldorMakeDirectoryOption;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.sdk.aldor.AldorSdkType;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.UnknownSdkType;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Objects;
import java.util.Optional;

/*
 * TODO: Java components should toggled
 */
public class AldorFacetEditorForm extends FacetPropertiesEditorTab<AldorModuleExtensionProperties, AldorFacetConfiguration> {
    private static final Logger LOG = Logger.getInstance(AldorFacetEditorForm.class);

    private JCheckBox buildJavaCheckBox;
    private JdkComboBox aldorSdkComboBox;
    private JdkComboBox javaSdkComboBox;
    private JPanel panel;
    private JTextField outputDirectoryField;
    private ProjectSdksModel model = null;

    public AldorFacetEditorForm(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        super(editorContext, validatorsManager);
        addAldorSdkValidator(validatorsManager);
        addJavaSdkValidator(validatorsManager);
    }

    private void addAldorSdkValidator(FacetValidatorsManager validatorsManager) {
        validatorsManager.registerValidator(new FacetEditorValidator() {
            @NotNull
            @Override
            public ValidationResult check() {
                return checkAldorSdk();
            }
        }, aldorSdkComboBox);
    }

    private void addJavaSdkValidator(FacetValidatorsManager validatorsManager) {
        validatorsManager.registerValidator(new FacetEditorValidator() {
            @NotNull
            @Override
            public ValidationResult check() {
                return checkJavaSdk();
            }
        }, javaSdkComboBox, buildJavaCheckBox);
    }

    private ValidationResult checkAldorSdk() {
        boolean isOK = (aldorSdkComboBox != null)
                && (aldorSdkComboBox.getSelectedJdk() != null)
                && Objects.equals(AldorInstalledSdkType.instance(), aldorSdkComboBox.getSelectedJdk().getSdkType());
        return isOK ? ValidationResult.OK: new ValidationResult("Aldor Version must be set");
    }

    private ValidationResult checkJavaSdk() {
        if (!buildJavaCheckBox.isSelected()) {
            return ValidationResult.OK;
        }
        boolean isOK = (javaSdkComboBox != null)
                && (javaSdkComboBox.getSelectedJdk() != null)
                && Objects.equals(JavaSdk.getInstance(), javaSdkComboBox.getSelectedJdk().getSdkType());
        return isOK ? ValidationResult.OK: new ValidationResult("Java SDK must be set if build java is enabled");
    }

    public void createUIComponents() {
        model = new ProjectSdksModel();
        model.reset(project());
        Condition<SdkTypeId> aldorSdkFilter = sdkType -> AldorInstalledSdkType.instance().equals(sdkType);
        Condition<SdkTypeId> javaSdkFilter = sdkType -> JavaSdk.getInstance().equals(sdkType);
        aldorSdkComboBox = new JdkComboBox(project(), model, aldorSdkFilter, null, aldorSdkFilter, this::sdkAdded);
        javaSdkComboBox = new JdkComboBox(project(), model, javaSdkFilter, null, javaSdkFilter, this::sdkAdded);
    }


    private void sdkAdded(Sdk sdk) {
        if ((sdk == null) || (model == null)) {
            return;
        }
        if (sdk.getSdkType() instanceof MissingSdkType) {
            return;
        }
        LOG.info("Adding SDK: " + sdk.getName());
        if (model.findSdk(sdk.getName()) != null) {
            LOG.info("SDK " + sdk.getName() + " already exists");
        }
        model.addSdk(sdk);
        try {
            model.apply(null, true);
        }
        catch (ConfigurationException e) {
            LOG.error("while creating SDK " + sdk.getName(), e);
        }
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(facetState(), currentState());
    }

    @Override
    public void apply() {
        facet().getConfiguration().updateState(currentState());
    }

    @Override
    @NotNull
    @VisibleForTesting
    public AldorModuleExtensionProperties currentState() {
        return AldorModuleExtensionProperties.builder()
            .setSdkName(Optional.ofNullable(aldorSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null))
            .setOutputDirectory(outputDirectoryField.getText())
            .setOption(JpsAldorMakeDirectoryOption.Source)
            .setBuildJavaComponents(buildJavaCheckBox.isSelected())
            .setJavaSdkName(Optional.ofNullable(javaSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null))
            .build();
    }

    @Override
    public void reset() {
        AldorModuleExtensionProperties state = facetState();
        if (state == null) {
            aldorSdkComboBox.setSelectedJdk(null);
            aldorSdkComboBox.setSelectedJdk(null);
            outputDirectoryField.setText("");
            buildJavaCheckBox.setSelected(false);
        }
        else {
            this.aldorSdkComboBox.setSelectedJdk(findSdk(AldorInstalledSdkType.instance(), state.sdkName()));
            this.javaSdkComboBox.setSelectedJdk(findSdk(JavaSdk.getInstance(), state.javaSdkName()));
            this.outputDirectoryField.setText(state.outputDirectory());
            this.buildJavaCheckBox.setSelected(facetState().buildJavaComponents());
        }
    }

    private Sdk findSdk(SdkType sdkType, String sdkName) {
        if (sdkName == null) {
            return null;
        }
        Sdk sdk = model.findSdk(sdkName);
        if (sdk == null) {
            LOG.info("Creating unknown sdk " + sdkName);
            sdk = model.createSdk(new MissingSdkType(AldorInstalledSdkType.instance()), sdkName, "");
            model.addSdk(sdk);
        }
        return sdk;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Aldor Settings";
    }

    @Override
    public void disposeUIResources() {
        if (model != null) {
            model.disposeUIResources();
        }
    }

    private static class MissingSdkType extends SdkType {
        private final SdkType sdkType;

        MissingSdkType(SdkType type) {
            super("Missing " + type.getName());
            this.sdkType = type;
        }

        @Override
        public @Nullable String suggestHomePath() {
            return null;
        }

        @Override
        public boolean isValidSdkHome(String path) {
            return false;
        }

        @Override
        public @NotNull String suggestSdkName(@Nullable String currentSdkName, String sdkHome) {
            throw new UnsupportedOperationException("suggest sdk name");
        }

        @Override @Nullable
        public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
            return null;
        }

        @Override
        public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
            return getName();
        }

        @Override
        public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
        }
    }

}