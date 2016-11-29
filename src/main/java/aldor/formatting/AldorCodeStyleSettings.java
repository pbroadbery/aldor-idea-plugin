package aldor.formatting;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class AldorCodeStyleSettings extends CustomCodeStyleSettings {
    public AldorCodeStyleSettings(CodeStyleSettings settings) {
        super("Aldor", settings);
    }

}
