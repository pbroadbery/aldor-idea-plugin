package aldor.ui;

import aldor.psi.AldorPsiUtils;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public class AldorTextAttributes {

    public static TextAttributesKey textAttributesForDefinitionClass(AldorPsiUtils.DefinitionClass defClass) {
        switch (defClass) {
            case CATEGORY:
                return TextAttributesKey.createTextAttributesKey("ALDOR_CATEGORY_NAME", DefaultLanguageHighlighterColors.LABEL);
            case DOMAIN:
                return TextAttributesKey.createTextAttributesKey("ALDOR_DOMAIN_NAME", DefaultLanguageHighlighterColors.CONSTANT);
            case VALUE:
                return TextAttributesKey.createTextAttributesKey("ALDOR_DOMAIN_NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
        }
        throw new RuntimeException("Missing");
    }
}
