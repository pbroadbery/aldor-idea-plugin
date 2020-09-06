package aldor.build.facet.aldor;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

@SuppressWarnings("serial")
public class AldorSdkComboBox extends ComboboxWithBrowseButton {
    private Project project = null;

    AldorSdkComboBox() {
        getComboBox().setRenderer(new AldorSdkListCellRenderer());
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private class AldorSdkListCellRenderer implements ListCellRenderer {
        public AldorSdkListCellRenderer() {
        }

        @Override
        @Nullable
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return null;
        }
    }
}
