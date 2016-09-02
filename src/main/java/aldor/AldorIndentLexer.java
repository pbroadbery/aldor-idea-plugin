package aldor;

import com.intellij.lexer.DelegateLexer;
import com.intellij.lexer.Lexer;
import org.jetbrains.annotations.NotNull;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Adds functionality to track indent width and pile mode.
 */
public class AldorIndentLexer extends DelegateLexer {
    private final IndentWidthCalculator indentCalculator = new IndentWidthCalculator();
    private final SortedMap<Integer, Integer> indentForPosition = new TreeMap<>();
    private boolean pileMode = false;

    public AldorIndentLexer(@NotNull Lexer delegate) {
        super(delegate);
    }

    public boolean pileMode() {
        return pileMode;
    }

    public int lastIndent() {
        return indentForPosition.get(this.getTokenStart());
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, startOffset, endOffset, initialState);
        this.pileMode = false;
        this.indentForPosition.clear();
    }

    @Override
    public void advance() {
        super.advance();
        //noinspection ObjectEquality
        if (getTokenType() == AldorTokenTypes.KW_Indent) {
            this.indentForPosition.put(getTokenStart(), indentCalculator.width(getTokenText()));
        }
        //noinspection ObjectEquality
        if (getTokenType() == AldorTokenTypes.KW_SysCmd) {
            this.pileMode = true;
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public SortedMap<Integer, Integer> indentMap() {
        return indentForPosition;
    }
}
