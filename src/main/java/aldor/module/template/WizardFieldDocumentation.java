package aldor.module.template;

import com.intellij.ide.util.projectWizard.WizardInputField;

import javax.swing.JTextPane;

public class WizardFieldDocumentation extends WizardInputField<JTextPane> {
    JTextPane pane;

    public WizardFieldDocumentation(String id, String text) {
        super(id, "");
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setText(text);
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public JTextPane getComponent() {
        return pane;
    }

    @Override
    public String getValue() {
        return "";
    }
}
