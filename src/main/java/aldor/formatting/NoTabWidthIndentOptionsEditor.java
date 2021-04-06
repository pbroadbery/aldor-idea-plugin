package aldor.formatting;

import com.intellij.application.options.IndentOptionsEditor;

/**
 * Aldor only supports a tab width of 8 characters, so re-use the standard
 * indent options with the tab width setting permanently disabled.
 */
public class NoTabWidthIndentOptionsEditor extends IndentOptionsEditor {

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

}
