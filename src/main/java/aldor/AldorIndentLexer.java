package aldor;

import com.intellij.lexer.DelegateLexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static aldor.AldorTokenTypes.KW_BackSet;
import static aldor.AldorTokenTypes.KW_Indent;
import static aldor.AldorTokenTypes.KW_SetTab;

/**
 * Adds functionality to track indent width and pile mode.
 */
public class AldorIndentLexer extends DelegateLexer {
    private final IndentWidthCalculator indentCalculator = new IndentWidthCalculator();
    private final Linearise lineariser = new Linearise();

    public AldorIndentLexer(@NotNull AldorLexerAdapter delegate) {
        super(delegate);
    }

    AldorLexerAdapter getAldorDelegate() {
        return (AldorLexerAdapter) getDelegate();
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, startOffset, endOffset, initialState);
        LexerPosition pos0 = getAldorDelegate().getCurrentPosition();
        lineariser.linearise(getAldorDelegate());
        getAldorDelegate().restore(pos0);
    }

    @Override
    public IElementType getTokenType() {
        IElementType tokType = super.getTokenType();
        if ((Objects.equals(tokType, KW_Indent)) && isAtBlockStart()) {
            return KW_SetTab;
        } else if ((Objects.equals(tokType, KW_Indent)) && isAtBlockNewLine()) {
            return KW_BackSet;
        }
        return tokType;
    }

    private boolean isAtBlockNewLine() {
        if (Objects.equals(getAldorDelegate().getTokenType(), KW_Indent)) {
            return lineariser.isAtBlockNewLine(getAldorDelegate().getTokenStart());
        } else {
            return false;
        }
    }

    private boolean isAtBlockStart() {
        if (Objects.equals(getAldorDelegate().getTokenType(), KW_Indent)) {
            return lineariser.isBlockStart(getAldorDelegate().getTokenStart());
        } else {
            return false;
        }
    }
}
