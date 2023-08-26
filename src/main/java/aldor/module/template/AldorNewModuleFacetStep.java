package aldor.module.template;

import aldor.builder.jps.module.AldorFacetProperties;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.util.concurrent.atomic.AtomicReference;

public class AldorNewModuleFacetStep extends ModuleWizardStep {
    private static final Logger LOG = Logger.getInstance(AldorNewModuleFacetStep.class);

    private final AldorNewModuleFacetForm form;
    @NotNull
    private final AtomicReference<AldorFacetProperties> properties;

    public AldorNewModuleFacetStep(Project project, @NotNull AtomicReference<AldorFacetProperties> properties) {
        this.form = new AldorNewModuleFacetForm(project);
        this.properties = properties;
    }

    @Override
    public JComponent getComponent() {
        return form.component();
    }

    @Override
    public void updateDataModel() {
        LOG.error("This is meaningless");
        AldorFacetProperties properties = properties();
    }

    @VisibleForTesting
    public void updateSdk(String sdkName) {
        AldorFacetProperties properties = properties();
        this.properties.set(properties.asBuilder().sdkName(sdkName).build());
    }

    private AldorFacetProperties properties() {
        return (properties.get() == null) ? new AldorFacetProperties() : properties.get();
    }

    private boolean createMakefiles() {
        return form.createMakefiles();
    }

}
