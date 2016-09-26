package aldor.language;

import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public final class SpadLanguage extends Language {

    public static final SpadLanguage INSTANCE = new SpadLanguage();

    private SpadLanguage() {
        super(AldorLanguage.INSTANCE, "Spad", "text/spad");
    }

}
