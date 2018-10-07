package aldor.spad.runconfiguration;

import aldor.spad.runconfiguration.SpadInputRunConfigurationType.SpadInputConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

class SpadInputConfigurable extends SettingsEditor<SpadInputConfiguration> {
    private final SpadInputConfigurableForm form;

    SpadInputConfigurable(Project project) {
        this.form = new SpadInputConfigurableForm(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull SpadInputConfiguration configuration) {
        form.resetEditor(configuration.bean());

    }

    @Override
    protected void applyEditorTo(@NotNull SpadInputConfiguration configuration) {
        SpadInputRunConfigurationType.SpadInputConfigurationBean bean = configuration.bean();
        form.updateConfiguration(bean);
        //configuration.setModule(bean.module); // TODO: Once module is set (and configuredSdk too...)
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return form.wholePanel();
    }
}
