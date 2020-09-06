package aldor.module.template;

import com.intellij.ide.util.projectWizard.WizardInputField;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WizardFieldContainer {
    private final Map<String, WizardInputField<?>> additionalFieldsByName;
    private final List<WizardInputField<?>> additionalFields;

    public WizardFieldContainer() {
        this.additionalFieldsByName = new HashMap<>();
        this.additionalFields = new ArrayList<>();
    }

    public void add(WizardInputField<?> field) {
        if (additionalFieldsByName.containsKey(field.getId())) {
            throw new IllegalArgumentException("Can't add the same id twice " + field.getId());
        }
        this.additionalFields.add(field);
        this.additionalFieldsByName.put(field.getId(), field);
    }

    public List<WizardInputField<?>> fields() {
        return additionalFields;
    }

    public <T> T value(Class<T> clss, String fieldId) {
        return clss.cast(field(fieldId).getValue());
    }

    public WizardInputField<?> field(String fieldId) {
        return additionalFieldsByName.get(fieldId);
    }
}
