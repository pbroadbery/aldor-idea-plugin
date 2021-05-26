package aldor.build.facet.aldor;

import aldor.build.facet.MissingSdkType;
import aldor.builder.jps.module.AldorFacetExtensionProperties;
import aldor.sdk.aldor.AldorInstalledSdkType;
import com.google.common.annotations.VisibleForTesting;
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
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.fields.ExtendableTextField;
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
public class AldorFacetEditorForm {
    private static final Logger LOG = Logger.getInstance(AldorFacetEditorForm.class);
    private AldorFacetEditor editor;

    private JCheckBox buildJavaCheckBox;
    private JdkComboBox aldorSdkComboBox;
    private JdkComboBox javaSdkComboBox;
    private JPanel panel;
    private ProjectSdksModel model = null;

    public AldorFacetEditorForm(AldorFacetEditor editor) {
        this.editor = editor;
    }


    private void registerAldorSdkValidator(FacetValidatorsManager validatorsManager) {
        validatorsManager.registerValidator(new FacetEditorValidator() {
            @NotNull
            @Override
            public ValidationResult check() {
                return checkAldorSdk();
            }
        }, aldorSdkComboBox);
    }

    private void registerJavaSdkValidator(FacetValidatorsManager validatorsManager) {
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
        try {
            model = new ProjectSdksModel();
            model.reset(editor.project());
            Condition<SdkTypeId> aldorSdkFilter = sdkType -> AldorInstalledSdkType.instance().equals(sdkType);
            Condition<SdkTypeId> javaSdkFilter = sdkType -> JavaSdk.getInstance().equals(sdkType);
            aldorSdkComboBox = new JdkComboBox(editor.project(), model, aldorSdkFilter, null, aldorSdkFilter, this::sdkAdded);
            javaSdkComboBox = new JdkComboBox(editor.project(), model, javaSdkFilter, null, javaSdkFilter, this::sdkAdded);
            registerAldorSdkValidator(editor.validatorsManager());
            registerJavaSdkValidator(editor.validatorsManager());
        }
        catch (Throwable e) {
            LOG.error(e);
            throw e;
        }
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
    @VisibleForTesting
    public AldorFacetExtensionProperties currentState() {
        return AldorFacetExtensionProperties.builder()
            .setSdkName(Optional.ofNullable(aldorSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null))
            .setBuildJavaComponents(buildJavaCheckBox.isSelected())
            .setJavaSdkName(Optional.ofNullable(javaSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null))
            .build();
    }

    public void reset() {
        AldorFacetExtensionProperties state = editor.facetState();
        if (state == null) {
            aldorSdkComboBox.setSelectedJdk(null);
            aldorSdkComboBox.setSelectedJdk(null);
            buildJavaCheckBox.setSelected(false);
        }
        else {
            this.aldorSdkComboBox.setSelectedJdk(findSdk(AldorInstalledSdkType.instance(), state.sdkName()));
            this.javaSdkComboBox.setSelectedJdk(findSdk(JavaSdk.getInstance(), state.javaSdkName()));
            this.buildJavaCheckBox.setSelected(state.buildJavaComponents());
        }
    }

    @Nullable
    private Sdk findSdk(SdkType sdkType, String sdkName) {
        if (sdkName == null) {
            return null;
        }
        Sdk sdk = model.findSdk(sdkName);
        if (sdk == null) {
            LOG.info("Creating unknown sdk " + sdkName);
            sdk = model.createSdk(new MissingSdkType(sdkType), sdkName, "");
            model.addSdk(sdk);
        }
        return sdk;
    }

    public void disposeUIResources() {
        if (model != null) {
            model.disposeUIResources();
        }
    }

    public JComponent topPanel() {
        return panel;
    }
}