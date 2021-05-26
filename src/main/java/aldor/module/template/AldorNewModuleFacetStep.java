package aldor.module.template;

import aldor.builder.jps.module.AldorFacetExtensionProperties;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import java.util.concurrent.atomic.AtomicReference;

public class AldorNewModuleFacetStep extends ModuleWizardStep {
    private static final Logger LOG = Logger.getInstance(AldorNewModuleFacetStep.class);

    private final AldorNewModuleFacetForm form;
    @Nonnull
    private final AtomicReference<AldorFacetExtensionProperties> properties;

    public AldorNewModuleFacetStep(Project project, @Nonnull AtomicReference<AldorFacetExtensionProperties> properties) {
        this.form = new AldorNewModuleFacetForm(project);
        this.properties = properties;
    }

    @Override
    public JComponent getComponent() {
        return form.component();
    }

    @Override
    public void updateDataModel() {
        AldorFacetExtensionProperties properties = properties();
    }

    @VisibleForTesting
    public void updateSdk(String sdkName) {
        AldorFacetExtensionProperties properties = properties();
        this.properties.set(properties.asBuilder().setSdkName(sdkName).build());
    }

    private AldorFacetExtensionProperties properties() {
        return (properties.get() == null) ? new AldorFacetExtensionProperties() : properties.get();
    }

    private boolean createMakefiles() {
        return form.createMakefiles();
    }

}
