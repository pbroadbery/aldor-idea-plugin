package aldor.list;

import com.intellij.lang.Language;

/**
 * Simple List Language specifier.
 */
public class ListLanguage extends Language {

    public static final Language INSTANCE = new ListLanguage();

    public ListLanguage() {
        super("List", "text/lists");
    }
}
