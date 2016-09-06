package aldor.highlighter;

import aldor.lexer.AldorLexerAdapter;
import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import com.google.common.collect.Maps;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class AldorSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] EMPTY_ATTRIBUTES = new TextAttributesKey[0];
    private final Map<IElementType, TextAttributesKey> aldorHighlightMap = Maps.newHashMap();

    AldorSyntaxHighlighter() {
        for (AldorTokenType tokenType : AldorTokenTypes.all()) {
            if (tokenType.isLangWord()) {
                aldorHighlightMap.put(tokenType, createTextAttributesKey("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD));
            }
        }
        aldorHighlightMap.put(AldorTokenTypes.TK_Comment, createTextAttributesKey("COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT));
        aldorHighlightMap.put(AldorTokenTypes.TK_PreDoc, createTextAttributesKey("PREDOC", DefaultLanguageHighlighterColors.DOC_COMMENT));
        aldorHighlightMap.put(AldorTokenTypes.TK_PostDoc, createTextAttributesKey("POSTDOC", DefaultLanguageHighlighterColors.DOC_COMMENT));

        aldorHighlightMap.put(AldorTokenTypes.TK_Int, createTextAttributesKey("NUMBER", DefaultLanguageHighlighterColors.NUMBER));
        aldorHighlightMap.put(AldorTokenTypes.TK_String, createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING));
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new AldorLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        final TextAttributesKey textAttributesKey = aldorHighlightMap.get(tokenType);
        if (textAttributesKey == null) {
            return EMPTY_ATTRIBUTES;
        }
        return new TextAttributesKey[]{textAttributesKey};
    }

}