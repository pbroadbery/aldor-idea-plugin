package aldor.expression;

import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public final class ExpressionLanguage extends Language {

    public static final ExpressionLanguage INSTANCE = new ExpressionLanguage();

    private ExpressionLanguage() {
        super("Expression", "text/expression");
    }

}
