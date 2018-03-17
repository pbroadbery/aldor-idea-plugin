package aldor.formatting;

import aldor.language.AldorLanguage;
import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.json.formatter.JsonCodeStyleSettings;
import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @NotNull
    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new MyCodeStyleConfigurable(settings, originalSettings);
    }

    @Nullable
    @Override
    public String getConfigurableDisplayName() {
        return AldorLanguage.INSTANCE.getDisplayName();
    }

    @Nullable
    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new JsonCodeStyleSettings(settings);
    }

    private static final class MyCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
        private MyCodeStyleConfigurable(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
            super(settings, originalSettings, "Aldor");
        }

        @Override
        protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings2) {
            final Language language = AldorLanguage.INSTANCE;
            final CodeStyleSettings currentSettings = getCurrentSettings();
            return new MyTabbedLanguageCodeStylePanel(language, currentSettings, settings2);
        }

        @Override
        public String getHelpTopic() {
            return "reference.settingsdialog.codestyle.json";
        }
    }

    private static final class MyTabbedLanguageCodeStylePanel extends TabbedLanguageCodeStylePanel{

        private MyTabbedLanguageCodeStylePanel(Language language, CodeStyleSettings currentSettings, CodeStyleSettings settings2) {
            super(language, currentSettings, settings2);
        }

        @Override
        protected void initTabs(CodeStyleSettings settings) {
            addIndentOptionsTab(settings);
                    /*
                    Additional formatting stuff for when we have the time...
                    addSpacesTab(settings);
                    addBlankLinesTab(settings);
                    addWrappingAndBracesTab(settings);
                    */
            addTab(new AldorCodeStylePanel(settings));
        }

    }
}
