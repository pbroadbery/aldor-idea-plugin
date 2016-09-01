package aldor;

import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public class AldorLanguage extends Language {

    public static final Language INSTANCE = new AldorLanguage();

    private AldorLanguage() {
        super("Aldor", "text/aldor");
    }
}
