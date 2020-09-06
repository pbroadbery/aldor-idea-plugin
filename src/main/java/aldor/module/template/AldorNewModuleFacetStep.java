package aldor.module.template;

import aldor.builder.jps.AldorModuleExtensionProperties;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import java.util.concurrent.atomic.AtomicReference;

public class AldorNewModuleFacetStep extends ModuleWizardStep {
    private static final Logger LOG = Logger.getInstance(AldorNewModuleFacetStep.class);

    private final AldorNewModuleFacetForm form;
    @Nonnull
    private final AtomicReference<AldorModuleExtensionProperties> properties;

    public AldorNewModuleFacetStep(Project project, @Nonnull AtomicReference<AldorModuleExtensionProperties> properties) {
        this.form = new AldorNewModuleFacetForm(project);
        this.properties = properties;
    }

    @Override
    public JComponent getComponent() {
        return form.component();
    }

    @Override
    public void updateDataModel() {
        AldorModuleExtensionProperties properties = properties();
        this.properties.set(properties.asBuilder().setSdkName(form.aldorSdkName()).build());
        LOG.info("Setting sdk to " + form.aldorSdkName());
    }

    private AldorModuleExtensionProperties properties() {
        return (properties.get() == null) ? new AldorModuleExtensionProperties() : properties.get();
    }
}
