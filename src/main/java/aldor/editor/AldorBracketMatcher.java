package aldor.editor;

import aldor.lexer.AldorTokenTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Matches brackets for Aldor.  Just the types below; nothing complicated.
 */
public class AldorBracketMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = {
            new BracePair(AldorTokenTypes.KW_OParen, AldorTokenTypes.KW_CParen, false),
            new BracePair(AldorTokenTypes.KW_OBBrack, AldorTokenTypes.KW_CBBrack, true),
            new BracePair(AldorTokenTypes.KW_OCurly, AldorTokenTypes.KW_CCurly, false),
    };
    @NotNull
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return false;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
