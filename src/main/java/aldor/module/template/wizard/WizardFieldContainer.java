package aldor.module.template.wizard;

import aldor.module.template.git.TypedWizardField;
import aldor.util.HasTypedValue;
import aldor.util.TypedName;
import com.intellij.ide.util.projectWizard.WizardInputField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public <T> T value(TypedName<T> name) {
        WizardInputField<?> field = field(name.name());
        if (field instanceof HasTypedValue<?>) {
            return name.clzz().cast(((HasTypedValue<?>) field).value());
        }
        else {
            return name.clzz().cast(field.getValue());
        }
    }

    @Override
    public String toString() {
        return "("+ additionalFieldsByName.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().getValue()).collect(Collectors.joining(",")) + ")";
    }

    @SuppressWarnings("unchecked")
    public <T> T field(TypedWizardField<T> name) {
        return (T) field(name.name());
    }
}
