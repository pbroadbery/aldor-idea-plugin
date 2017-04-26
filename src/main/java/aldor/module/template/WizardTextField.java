package aldor.module.template;

import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.ValidatingTextField;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.util.function.Function;

public class WizardTextField extends WizardInputField<JComponent> {
    private final String label;
    private final ValidatingTextField field;
    private final Function<String, String> validator;

    public WizardTextField(String id, String label, String defaultValue, Function<String, String> validator) {
        super(id, defaultValue);
        this.label = label;
        //noinspection serial
        this.field = new ValidatingTextField(new JTextField(defaultValue)) {
            @Override
            protected String validateTextOnChange(String text, DocumentEvent e) {
                //noinspection SerializableStoresNonSerializable
                String msg = validator.apply(text);
                return (msg == null) ? "" : msg;
            }
        };
        this.validator = validator;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public JComponent getComponent() {
        return field;
    }

    @Override
    public String getValue() {
        return field.getText();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        String value = getValue();
        if ((value != null) && (value.isEmpty())) {
            throw new IllegalStateException("Validator should not return an empty error message");
        }
        return validator.apply(value) == null;
    }
}
