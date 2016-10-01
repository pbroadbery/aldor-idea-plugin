package aldor.build.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.Objects;

/**
 * Created by pab on 27/09/16.
 */
public class AldorFacetEditorTab extends FacetEditorTab {
    private final FacetEditorContext editorContext;
    private JPanel myMainPanel;
    private JTextField buildDirectory;

    public AldorFacetEditorTab(final FacetEditorContext editorContext) {
        this.editorContext = editorContext;
    }

    AldorFacetConfig config() {
        return (AldorFacetConfig) editorContext.getFacet().getConfiguration();
    }

    @Override
    public void apply() throws ConfigurationException {
        System.out.println("Apply: " + config().buildDirectory() + " " + buildDirectory.getText());
        config().buildDirectory(buildDirectory.getText());
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        String text = buildDirectory.getText();
        return !Objects.equals(config().buildDirectory(), text);
    }

    @Override
    public void reset() {
        System.out.println("Reset!: " + config().buildDirectory() + " " + buildDirectory.getText());
        buildDirectory.setText(config().buildDirectory());
    }

    @Override
    public void disposeUIResources() {

    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Aldor Facet Configuration";
    }
}
