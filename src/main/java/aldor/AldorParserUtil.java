package aldor;

import aldor.lexer.AldorTokenTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static aldor.lexer.AldorTokenTypes.KW_BackSet;
import static aldor.lexer.AldorTokenTypes.KW_Semicolon;

@SuppressWarnings({"ExtendsUtilityClass", "StaticMethodOnlyUsedInOneClass"})
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
        if (builder.getTokenType() == KW_BackSet) {
            return consumeToken(builder, KW_BackSet);
        }
        return consumeToken(builder, KW_Semicolon);
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
            } else if (!AldorParserDefinition.WHITE_SPACES.contains(elt)
                        && (elt != AldorTokenTypes.TK_PreDoc) && (elt != AldorTokenTypes.TK_PostDoc)) {
                done = true;
            }
            idx--;
        }
        return elt;
    }

    public static boolean backSet(@NotNull PsiBuilder builder, @SuppressWarnings("UnusedParameters") int level) {
        return false;
    }

    public static boolean backTab(@NotNull PsiBuilder builder, @SuppressWarnings("UnusedParameters") int level) {
        return true;
    }

    /*
        Piled_Expression ::=  KW_SetTab PileContents_Expression KW_BackTab
        PileContents_Expression ::= (Doc_Expression | error KW_BackSet Doc_Expression) (<<parseBackset>> Doc_Expression)*


        if foo(
            ....) then
           blah()

    */



}
