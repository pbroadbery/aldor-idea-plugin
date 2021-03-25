package aldor.build.facet.fricas;

import aldor.build.facet.FacetPropertiesEditorTab;
import aldor.sdk.fricas.FricasInstalledSdkType;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FricasFacetEditorForm extends FacetPropertiesEditorTab<FricasFacetProperties, FricasFacetConfiguration> {
    private static final Logger LOG = Logger.getInstance(FricasFacetEditorForm.class);
    private JdkComboBox fricasSdkComboBox;
    private JPanel panel;
    private JTextPane thisIsTheLocationTextPane;
    @Nullable
    private ProjectSdksModel model;

    public FricasFacetEditorForm(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        super(editorContext, validatorsManager);
        addFricasSdkValidator(validatorsManager);
    }

    @Override
    public FricasFacetProperties currentState() {
        return new FricasFacetProperties(Optional.ofNullable(fricasSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null));
    }

    @Override
    public void reset() {
        FricasFacetProperties state = facetState();

        if ((state == null) || (state.sdkName() == null)) {
            LOG.info("No state for facet " + facet().getName());
            fricasSdkComboBox.setSelectedJdk(null);
        }
        else {
            LOG.info("Setting state to " + state.sdkName() + " sdk " + model.findSdk(state.sdkName())
                    + " " + Arrays.stream(model.getSdks()).map(Sdk::getName).collect(Collectors.joining(", ")));
            fricasSdkComboBox.setSelectedJdk(model.findSdk(state.sdkName()));
        }
    }

    private void addFricasSdkValidator(FacetValidatorsManager validatorsManager) {
        validatorsManager.registerValidator(new FacetEditorValidator() {
            @NotNull
            @Override
            public ValidationResult check() {
                return checkFricasSdk();
            }
        }, fricasSdkComboBox);
    }

    private ValidationResult checkFricasSdk() {
        boolean isOK = (fricasSdkComboBox != null)
                && (fricasSdkComboBox.getSelectedJdk() != null)
                && Objects.equals(FricasInstalledSdkType.instance(), fricasSdkComboBox.getSelectedJdk().getSdkType());
        return isOK ? ValidationResult.OK: new ValidationResult("Fricas directory must be set");
    }


    @Override
    public void apply() throws ConfigurationException {
        facet().getConfiguration().updateState(new FricasFacetProperties(Optional.ofNullable(fricasSdkComboBox.getSelectedJdk()).map(Sdk::getName).orElse(null)));
    }

    @Override
    public @NotNull JComponent createComponent() {
        return panel;
    }

    public void createUIComponents() {
        LOG.info("Creating UI component for fricas facet editor");
        model = new ProjectSdksModel();
        model.syncSdks();
        Condition<SdkTypeId> fricasSdkFilter = sdkType -> FricasInstalledSdkType.instance().equals(sdkType);
        fricasSdkComboBox = new JdkComboBox(project(), model, fricasSdkFilter, null, fricasSdkFilter, this::sdkAdded);
    }

    private void sdkAdded(Sdk sdk) {
        LOG.info("Adding SDK: " + sdk.getName());
        if (ProjectJdkTable.getInstance().findJdk(sdk.getName()) == null) {
            ProjectJdkTable.getInstance().addJdk(sdk);
        }
        else {
            LOG.warn("SDK " + sdk.getName() + " has already been created");
        }
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Fricas Settings";
    }
}
