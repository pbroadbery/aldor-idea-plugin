package aldor.runconfiguration.spad;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

class SpadInputConfigurable extends SettingsEditor<SpadInputConfiguration> {
    @NotNull
    private final SpadInputConfigurableForm form;

    SpadInputConfigurable(Project project, @Nullable Module module, Sdk sdk) {
        this.form = new SpadInputConfigurableForm(project, module, sdk);
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

    @NotNull
    SpadInputConfigurableForm form() {
        return form;
    }
}
