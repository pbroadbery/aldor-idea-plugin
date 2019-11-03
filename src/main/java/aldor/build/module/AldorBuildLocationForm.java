package aldor.build.module;

import javax.swing.JComponent;
import javax.swing.JTextField;

@SuppressWarnings({"serial", "SerializableHasSerializationMethods"})
public class AldorBuildLocationForm extends JComponent {
    private JTextField buildDirectory;

    public String buildDirectory() {
        return buildDirectory.getText();
    }
}
