package aldor.lexer;

import aldor.AldorLanguage;
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
    private final boolean isOpener;
    private final boolean isCloser;
    private final boolean isFollower;

    @SuppressWarnings("UnusedParameters")
    public AldorTokenType(@NotNull String name, int i, @NotNull  String text, int hasString, int isComment, int isOpener, int isCloser,
                          int isFollower, int isLangWord, int isLeftAssoc, int isMaybeInfix, int precedence, int isDisabled) {
        super(name, AldorLanguage.INSTANCE);
        this.name = name;
        this.text = text;
        this.isLangWord = isLangWord != 0;
        this.isOpener = isOpener != 0;
        this.isCloser = isCloser != 0;
        this.isFollower = isFollower != 0;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String text() {
        return text;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return AldorLanguage.INSTANCE;
    }

    public boolean isLangWord() {
        return this.isLangWord;
    }

    public boolean isOpener() {
        return isOpener;
    }

    public boolean isCloser() {
        return isCloser;
    }

    public boolean isFollower() {
        return isFollower;
    }

}
