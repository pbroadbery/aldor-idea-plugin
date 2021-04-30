package aldor.build.facet.aldor;

import aldor.build.facet.FacetPropertiesEditorTab;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.builder.jps.JpsAldorMakeDirectoryOption;
import aldor.sdk.aldor.AldorInstalledSdkType;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.fields.ExtendableTextField;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
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
    private FieldPanel outputDirectoryFieldPanel;
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

        outputDirectoryFieldPanel = createDirectoryFieldPanel(this::updateOutputDirectory);
    }

    private void updateOutputDirectory() {

    }


    private void sdkAdded(Sdk sdk) {
        LOG.info("Adding SDK: " + sdk.getName());
        if (ProjectJdkTable.getInstance().findJdk(sdk.getName()) == null) {
            ApplicationManager.getApplication().runWriteAction(() -> ProjectJdkTable.getInstance().addJdk(sdk));
        }
        else {
            LOG.warn("SDK " + sdk.getName() + " has already been created");
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
            .setOutputDirectory(outputDirectoryFieldPanel.getText())
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
            outputDirectoryField.setText(Optional.ofNullable(ProjectUtil.guessModuleDir(module())).map(x -> x.getPath() + "/out").orElse(""));
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
        @Nullable
        public String suggestHomePath() {
            return null;
        }

        @Override
        public boolean isValidSdkHome(String path) {
            return false;
        }

        @Override
        @NotNull
        public String suggestSdkName(@Nullable String currentSdkName, String sdkHome) {
            throw new UnsupportedOperationException("suggest sdk name");
        }

        @Override @Nullable
        public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
            return null;
        }

        @Override
        @NotNull
        @Nls(capitalization = Nls.Capitalization.Title)
        public String getPresentableName() {
            return getName();
        }

        @Override
        public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
        }
    }

    FieldPanel createDirectoryFieldPanel(Runnable commit) {
        JTextField field = new ExtendableTextField();
        final FileChooserDescriptor outputPathsChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        outputPathsChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, module());
        outputPathsChooserDescriptor.setHideIgnored(false);
        InsertPathAction.addTo(field, outputPathsChooserDescriptor);
        FileChooserFactory.getInstance().installFileCompletion(field, outputPathsChooserDescriptor, true, null);

        Runnable docListener = () -> {LOG.info("Document changed"); commit.run();};

        return new FieldPanel(field, null, null,
                new BrowseFilesListener(field, "Title", "Description", outputPathsChooserDescriptor) {
            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                commit.run();
            }
        }, docListener);
    }
}