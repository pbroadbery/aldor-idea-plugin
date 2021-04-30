package aldor.language;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * Information about the AldorLanguage
 */
public final class SpadLanguage extends Language {
    public static final SpadLanguage INSTANCE = new SpadLanguage();

    private SpadLanguage() {
        super(AldorLanguage.INSTANCE, "Spad", "text/spad");
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return "Spad";
    }
}
