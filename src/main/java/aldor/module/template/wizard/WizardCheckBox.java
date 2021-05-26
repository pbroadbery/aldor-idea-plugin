package aldor.module.template.wizard;

import aldor.util.HasTypedValue;
import com.intellij.ide.util.projectWizard.WizardInputField;

import javax.swing.JCheckBox;

public class WizardCheckBox extends WizardInputField<JCheckBox> implements HasTypedValue<Boolean> {

    private final String label;
    private final JCheckBox checkbox;

    @SuppressWarnings("BooleanParameter")
    public WizardCheckBox(String id, String label, boolean defaultValue) {
        super(id, Boolean.toString(defaultValue));
        this.label = label;
        this.checkbox = new JCheckBox();
        this.checkbox.setSelected(defaultValue);
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

    public boolean isSelected() {
        return checkbox.isSelected();
    }

    @Override
    public Boolean value() {
        return checkbox.isSelected();
    }
}
