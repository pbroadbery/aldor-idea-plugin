package aldor.module.template;

import com.intellij.ide.util.projectWizard.WizardInputField;

import javax.swing.JCheckBox;

public class WizardCheckBox extends WizardInputField<JCheckBox> {

    private final String label;
    private final JCheckBox checkbox;

    @SuppressWarnings("BooleanParameter")
    public WizardCheckBox(String id, String label, boolean defaultValue) {
        super(id, Boolean.toString(defaultValue));
        this.label = label;
        this.checkbox = new JCheckBox("", defaultValue);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public JCheckBox getComponent() {
        return checkbox;
    }

    @Override
    public String getValue() {
        return Boolean.toString(checkbox.isSelected());
    }
}
