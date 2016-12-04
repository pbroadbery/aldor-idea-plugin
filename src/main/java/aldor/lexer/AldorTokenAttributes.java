package aldor.lexer;

public class AldorTokenAttributes {
    private final int i;
    private final String text;
    private final int hasString;
    private final int isComment;
    private final int isOpener;
    private final int isCloser;
    private final int isFollower;
    private final int isLangword;
    private final int isLeftAssoc;
    private final int isMaybeInfix;
    private final int precedence;
    private final int isDisabled;

    public AldorTokenAttributes(int i, String text, int hasString, int isComment, int isOpener, int isCloser, int isFollower, int isLangword, int isLeftAssoc, int isMaybeInfix, int precedence, int isDisabled) {
        this.i = i;
        this.text = text;
        this.hasString = hasString;
        this.isComment = isComment;
        this.isOpener = isOpener;
        this.isCloser = isCloser;
        this.isFollower = isFollower;
        this.isLangword = isLangword;
        this.isLeftAssoc = isLeftAssoc;
        this.isMaybeInfix = isMaybeInfix;
        this.precedence = precedence;
        this.isDisabled = isDisabled;
    }

    public int getI() {
        return i;
    }

    public String getText() {
        return text;
    }

    public int getHasString() {
        return hasString;
    }

    public int getIsComment() {
        return isComment;
    }

    public int getIsOpener() {
        return isOpener;
    }

    public int getIsCloser() {
        return isCloser;
    }

    public int getIsFollower() {
        return isFollower;
    }

    public int getIsLangword() {
        return isLangword;
    }

    public int getIsLeftAssoc() {
        return isLeftAssoc;
    }

    public int getIsMaybeInfix() {
        return isMaybeInfix;
    }

    public int getPrecedence() {
        return precedence;
    }

    public int getIsDisabled() {
        return isDisabled;
    }
}
