package aldor;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class AldorParserUtil extends GeneratedParserUtilBase {

    /*
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
        builder = GeneratedParserUtilBase.adapt_builder_(root, builder, parser, extendsSets);
        builder = new AldorPsiBuilderAdapter(builder);
        return builder;
    }

    static class AldorPsiBuilderAdapter extends PsiBuilderAdapter {
        private boolean lastWasSemicolon;

        public AldorPsiBuilderAdapter(PsiBuilder delegate) {
            super(delegate);
        }

    }
    */

    public static boolean semicolonOrCloseBraceNearby(@NotNull PsiBuilder builder, @SuppressWarnings("UnusedParameters") int level) {
        IElementType prevElt = skipDocsAndComments(builder);
        if (prevElt == AldorTokenTypes.KW_CCurly) {
            return true;
        }
        return consumeToken(builder, AldorTokenTypes.KW_Semicolon);
    }

    private static IElementType skipDocsAndComments(@NotNull  PsiBuilder builder) {
        int idx = -1;
        boolean done = false;
        IElementType elt = null;
        while (!done) {
            elt = builder.rawLookup(idx);
            if (elt == null)
                done = true;
            else if (elt != AldorTokenTypes.TK_Comment && elt != AldorTokenTypes.WHITE_SPACE && elt != AldorTokenTypes.TK_PreDoc && elt != AldorTokenTypes.TK_PostDoc)
                done = true;
            idx--;
        }
        return elt;
    }

}
