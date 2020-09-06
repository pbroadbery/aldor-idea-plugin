package aldor.module.template;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @deprecated Using settings
 */
@Deprecated
public class FricasSdkForm {
    private final Project project;
    private JdkComboBox jdkComboBox1;
    private JPanel panel;

    public FricasSdkForm(Project project) {
        this.project = project;
    }

    JComponent getPanel() {
        return this.panel;
    }

    @Nullable
    public Sdk sdk() {
        return jdkComboBox1.getSelectedJdk();
    }

    public void createUIComponents() {

    }
}
