package aldor.build.module;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("aldorEnabled")
public class AldorEnabledModuleState {
    @Attribute
    public boolean aldorEnabled;

    public AldorEnabledModuleState() {
        this.aldorEnabled = false;
    }

    @SuppressWarnings("BooleanParameter")
    public AldorEnabledModuleState(boolean aldorEnabled) {
        this.aldorEnabled = aldorEnabled;
    }
}
