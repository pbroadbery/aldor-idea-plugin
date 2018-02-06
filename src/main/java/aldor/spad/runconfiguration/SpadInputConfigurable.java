package aldor.spad.runconfiguration;

import aldor.spad.runconfiguration.SpadInputRunConfigurationType.SpadInputConfiguration;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

class SpadInputConfigurable extends SettingsEditor<SpadInputConfiguration> {
    private final Project project;
    private final SpadInputConfigurableForm form;

    public SpadInputConfigurable(Project project) {
        this.project = project;
        this.form = new SpadInputConfigurableForm(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull SpadInputConfiguration configuration) {
        form.resetEditor(configuration.bean());

    }

    @Override
    protected void applyEditorTo(@NotNull SpadInputConfiguration configuration) throws ConfigurationException {
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
