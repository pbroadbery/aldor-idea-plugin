package aldor.module.template;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;

import javax.swing.JComponent;

@Deprecated // Not sure we want to use this yet
public class FricasSdkWizardStep extends ModuleWizardStep {
    private final WizardContext context;
    private final FricasSimpleModuleBuilder builder;
    private FricasSdkForm form;

    FricasSdkWizardStep(WizardContext context, FricasSimpleModuleBuilder builder, Disposable parentDisposable) {
        this.form = new FricasSdkForm(context.getProject());
        this.context = context;
        this.builder = builder;
    }

    @Override
    public JComponent getComponent() {
        return form.getPanel();
    }

    @Override
    public void updateDataModel() {

    }


}
