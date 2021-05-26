package aldor.module.template.wizard;

import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.valueEditors.TextFieldValueEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.function.Function;

public class WizardTextField extends WizardInputField<JComponent> {
    private final String label;
    private final ValidatingTextField field;
    private final Function<String, String> validator;

    public WizardTextField(String id, String label, String defaultValue, Function<String, String> validator) {
        super(id, defaultValue);
        this.label = label;
        TextFieldValueEditor<String> valueEditor = new ValidatingValueEditor(defaultValue, label, validator);
        this.field = new ValidatingTextField(valueEditor);
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
        return field.getValue();
    }

    @Override
    public boolean validate() {
        String value = getValue();
        if ((value != null) && (value.isEmpty())) {
            throw new IllegalStateException("Validator should not return an empty error message");
        }
        return validator.apply(value) == null;
    }

    private static class ValidatingValueEditor extends TextFieldValueEditor<String> {

        private final Function<String, String> validator;

        ValidatingValueEditor(String defaultValue, String label, Function<String, String> validator) {
            super(new JTextField(defaultValue), label, defaultValue);
            this.validator = validator;
        }

        @NotNull
        @Override
        public String parseValue(@Nullable String s) {
            return (s == null) ? "" : s;
        }

        @Override
        public String valueToString(@NotNull String s) {
            return s;
        }

        @Override
        public boolean isValid(@NotNull String s) {
            return validator.apply(s) == null;
        }
    }

    @SuppressWarnings({"serial", "SerializableHasSerializationMethods"})
    private static class ValidatingTextField extends JBTextField {
        private final TextFieldValueEditor<String> myValueEditor;

        ValidatingTextField(TextFieldValueEditor<String> editor) {
            this.myValueEditor = editor;
        }

        public void setValue(@NotNull String newValue) {
            myValueEditor.setValue(newValue);
        }

        @NotNull
        public String getValue() {
            return myValueEditor.getValue();
        }

        public void setDefaultValue(@NotNull String defaultValue) {
            myValueEditor.setDefaultValue(defaultValue);
        }

        @NotNull
        public String getDefaultValue() {
            return myValueEditor.getDefaultValue();
        }

        @Nullable
        public String getValueName() {
            return myValueEditor.getValueName();
        }

    }
}
