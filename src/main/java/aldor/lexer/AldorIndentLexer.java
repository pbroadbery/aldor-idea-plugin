package aldor.lexer;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.lexer.DelegateLexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static aldor.lexer.AldorTokenTypes.KW_BlkEnd;
import static aldor.lexer.AldorTokenTypes.KW_BlkNext;
import static aldor.lexer.AldorTokenTypes.KW_BlkStart;
import static aldor.lexer.AldorTokenTypes.KW_EndPile;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.TK_SysCmd;

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

    @VisibleForTesting
    public Linearise lineariser() {
        return lineariser;
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
        if ((Objects.equals(tokType, KW_NewLine)) && isAtBlockStart()) {
            return KW_BlkStart;
        }
        else if ((Objects.equals(tokType, KW_NewLine)) && isAtBlockEnd()) {
            return KW_BlkEnd;
        }
        else if ((Objects.equals(tokType, KW_NewLine)) && isAtBlockNewLine()) {
            return KW_BlkNext;
        }
        else if (Objects.equals(tokType, TK_SysCmd)) {
            if ("#pile".equals(getTokenText())) {
                return AldorTokenTypes.KW_StartPile;
            }
            else if ("#unpile".equals(getTokenText())) {
                return KW_EndPile;
            }
        }
        return tokType;
    }

    private boolean isAtBlockNewLine() {
        if (Objects.equals(getAldorDelegate().getTokenType(), KW_NewLine)
                && lineariser.isPileMode(getAldorDelegate().getTokenStart())) {
            return lineariser.isAtBlockNewLine(getAldorDelegate().getTokenEnd());
        } else {
            return false;
        }
    }

    private boolean isAtBlockStart() {
        if (Objects.equals(getAldorDelegate().getTokenType(), KW_NewLine)
                && lineariser.isPileMode(getAldorDelegate().getTokenStart())) {
            return lineariser.isBlockStart(getAldorDelegate().getTokenEnd());
        } else {
            return false;
        }
    }

    private boolean isAtBlockEnd() {
        if (Objects.equals(getAldorDelegate().getTokenType(), KW_NewLine)
                && lineariser.isPileMode(getAldorDelegate().getTokenStart())) {
            return lineariser.isBlockEnd(getAldorDelegate().getTokenEnd());
        } else {
            return false;
        }
    }

    public int indentLevel(int c) {
        return lineariser.indentLevel(c);
    }
}
