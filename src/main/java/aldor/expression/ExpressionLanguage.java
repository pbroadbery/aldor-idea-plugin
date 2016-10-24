package aldor.expression;

import aldor.language.AldorLanguage;
import com.intellij.lang.Language;

/**
 * Information about the AldorLanguage
 */
public final class ExpressionLanguage extends Language {

    public static final ExpressionLanguage INSTANCE = new ExpressionLanguage();

    private ExpressionLanguage() {
        super(AldorLanguage.INSTANCE, "Expression", "text/expression");
    }

}
