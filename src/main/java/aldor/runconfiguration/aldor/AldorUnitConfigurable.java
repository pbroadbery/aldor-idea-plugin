package aldor.runconfiguration.aldor;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class AldorUnitConfigurable extends SettingsEditor<AldorUnitConfiguration> {
    private final AldorUnitConfigurableForm form;

    AldorUnitConfigurable(Project project) {
        this.form = new AldorUnitConfigurableForm(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull AldorUnitConfiguration configuration) {
        form.resetEditor(configuration);

    }

    @Override
    protected void applyEditorTo(@NotNull AldorUnitConfiguration configuration) {
        form.updateConfiguration(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return form.wholePanel();
    }

}
