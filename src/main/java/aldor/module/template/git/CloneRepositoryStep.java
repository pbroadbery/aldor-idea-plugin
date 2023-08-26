package aldor.module.template.git;

import aldor.module.template.wizard.WizardFieldContainer;
import aldor.util.TypedTry;
import com.intellij.ide.util.projectWizard.AbstractStepWithProgress;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Optional;

class CloneRepositoryStep extends AbstractStepWithProgress<AldorGitModuleBuilder.CloneResult> {
    private static final Logger LOG = Logger.getInstance(CloneRepositoryStep.class);
    private final GitModuleDetail detail;
    private final WizardContext wizardContext;
    private final WizardFieldContainer fields;

    public CloneRepositoryStep(@NotNull WizardContext wizardContext,
                               @NotNull ModulesProvider modulesProvider,
                               WizardFieldContainer fields,
                               GitModuleDetail detail) {
        super("Cloning repository...");
        LOG.info(new Throwable("Creating clone repo step.."));
        this.wizardContext = wizardContext;
        this.detail = detail;
        this.fields = fields;
    }

    @Override
    protected JComponent createResultsPanel() {
        return new JPanel();
    }

    @Override
    protected @NlsContexts.ProgressText String getProgressText() {
        return "Cloning... ";
    }

    @Override
    protected boolean shouldRunProgress() {
        return true;
    }

    @Override
    protected AldorGitModuleBuilder.CloneResult calculate() {
        Application app = ApplicationManager.getApplication();
        LOG.info("Calculate.. thread: " + app.isDispatchThread() + " write: " + app.isWriteThread() + " read: " + app.isReadAccessAllowed());

        TypedTry<Boolean, ConfigurationException> result = TypedTry.of(ConfigurationException.class,
                () -> {
                    detail.doClone(Optional.ofNullable(wizardContext.getProject()).orElse(ProjectManager.getInstance().getDefaultProject()));
                    return true;
                });
        LOG.info("Cloned! " + result);
        result.onFailure( e -> LOG.error(e));

        return result.map(x -> x ? AldorGitModuleBuilder.CloneResult.OK : AldorGitModuleBuilder.CloneResult.FAIL).orElse(AldorGitModuleBuilder.CloneResult.FAIL);
    }

    @Override
    protected void onFinished(@Nullable AldorGitModuleBuilder.CloneResult o, boolean canceled) {
        Application app = ApplicationManager.getApplication();
        LOG.info("OnFinished thread: " + app.isDispatchThread() + " write: " + app.isWriteThread() + " read: " + app.isReadAccessAllowed());
        LOG.info("Finished!");
    }

    @Override
    public void updateDataModel() {
        Application app = ApplicationManager.getApplication();
        LOG.info("Update Data Model: Dispatch thread: " + app.isDispatchThread() + " write: " + app.isWriteThread() + " read: " + app.isReadAccessAllowed());
        LOG.info("Updating datamodel");
        LOG.info(new Throwable("Just to prove I'm here"));
    }
}
