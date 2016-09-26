package aldor;

import aldor.references.FileScopeWalker;
import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public final class AldorLanguage extends Language {

    public static final AldorLanguage INSTANCE = new AldorLanguage();
    private final FileScopeWalker walker = new FileScopeWalker();

    private AldorLanguage() {
        super("Aldor", "text/aldor");
    }

    public FileScopeWalker scopeWalker() {
        return walker;
    }
}
