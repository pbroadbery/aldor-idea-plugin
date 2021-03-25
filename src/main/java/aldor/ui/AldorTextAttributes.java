package aldor.ui;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

public final class AldorTextAttributes {
    public static final TextAttributesKey ALDOR_CATEGORY_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_CATEGORY_NAME", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey ALDOR_DOMAIN_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_DOMAIN_NAME", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey ALDOR_VALUE_ATTRIBUTES = TextAttributesKey.createTextAttributesKey("ALDOR_VALUE_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final @NotNull SimpleTextAttributes CONDITION_ATTRIBUTES = SimpleTextAttributes.fromTextAttributes(SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES.toTextAttributes());


}
