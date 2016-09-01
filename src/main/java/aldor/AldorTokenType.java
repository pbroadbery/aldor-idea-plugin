package aldor;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Basically a token.  Contains top facts from the C code.
 */
public class AldorTokenType extends IElementType {
    private final String name;
    private final String text;
    private final boolean isLangWord;
    @SuppressWarnings("UnusedParameters")
    public AldorTokenType(String name, int i, String text, int hasString, int isComment, int isOpener, int isCloser,
                          int isFollower, int isLangWord, int isLeftAssoc, int isMaybeInfix, int precedence, int isDisabled) {
        super(name, AldorLanguage.INSTANCE);
        this.name = name;
        this.text = text;
        this.isLangWord = isLangWord != 0;
    }

    public AldorTokenType(String token) {
        super(token, AldorLanguage.INSTANCE);
        this.name = token;
        this.text = "";
        this.isLangWord = false;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return AldorLanguage.INSTANCE;
    }

    public boolean isLangWord() {
        return this.isLangWord;
    }
}
