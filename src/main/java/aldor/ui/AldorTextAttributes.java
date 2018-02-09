package aldor.ui;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class AldorTextAttributes {
    public static final TextAttributesKey ALDOR_CATEGORY_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_CATEGORY_NAME", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey ALDOR_DOMAIN_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_DOMAIN_NAME", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ALDOR_VALUE_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_VALUE_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);

}
