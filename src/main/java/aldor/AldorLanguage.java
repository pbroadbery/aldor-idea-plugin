package aldor;

import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public final class AldorLanguage extends Language {

    public static final AldorLanguage INSTANCE = new AldorLanguage();

    private AldorLanguage() {
        super("Aldor", "text/aldor");
    }

}
