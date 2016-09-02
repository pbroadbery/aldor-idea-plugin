package aldor;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static aldor.AldorTypes.KW_BACKTAB;
import static aldor.AldorTypes.KW_SETTAB;
import static aldor.AldorTypes.PILED_EXPRESSION;

@SuppressWarnings("ExtendsUtilityClass")
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
    // Return true if last token was a close brace, or looking at a semicolon
    // Used to determine if there is a logical semicolon - ie. statement terminator
    // at the current position.
    @SuppressWarnings("ObjectEquality")
    public static boolean semicolonOrCloseBraceNearby(@NotNull PsiBuilder builder, @SuppressWarnings("UnusedParameters") int level) {
        IElementType prevElt = skipDocsAndComments(builder);
        if (prevElt == AldorTokenTypes.KW_CCurly) {
            return true;
        }
        return consumeToken(builder, AldorTokenTypes.KW_Semicolon);
    }

    @SuppressWarnings("ObjectEquality")
    private static IElementType skipDocsAndComments(@NotNull  PsiBuilder builder) {
        int idx = -1;
        boolean done = false;
        IElementType elt = null;
        while (!done) {
            elt = builder.rawLookup(idx);
            if (elt == null) {
                done = true;
            } else if ((elt != AldorTokenTypes.TK_Comment) && (elt != AldorTokenTypes.WHITE_SPACE) && (elt != AldorTokenTypes.TK_PreDoc) && (elt != AldorTokenTypes.TK_PostDoc)) {
                done = true;
            }
            idx--;
        }
        return elt;
    }


    /*
        Piled_Expression ::= <<parsePiledExpression>>
        KW_SetTab PileContents_Expression KW_BackTab
    */
    public static boolean parsePiledExpression(PsiBuilder builder, int level, @SuppressWarnings("UnusedParameters") AldorParser parser) {
        if (!recursion_guard_(builder, level, "Piled_Expression")) {
            return false;
        }
        if (!nextTokenIs(builder, KW_SETTAB)) {
            return false;
        }
        PsiBuilder.Marker m = enter_section_(builder);
        boolean r = consumeToken(builder, KW_SETTAB);
        r = r && AldorParser.PileContents_Expression(builder, level + 1);
        r = r && consumeToken(builder, KW_BACKTAB);
        exit_section_(builder, m, PILED_EXPRESSION, r);
        return r;


    }

}
