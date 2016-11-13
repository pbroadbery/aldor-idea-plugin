package aldor.parser;

import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorTokenTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static aldor.lexer.AldorTokenTypes.KW_BlkEnd;
import static aldor.lexer.AldorTokenTypes.KW_BlkNext;
import static aldor.lexer.AldorTokenTypes.KW_Repeat;
import static aldor.lexer.AldorTokenTypes.KW_Semicolon;

@SuppressWarnings({"ExtendsUtilityClass", "StaticMethodOnlyUsedInOneClass"})
public class AldorParserUtil extends GeneratedParserUtilBase {

    /*
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
        return adapted;
    }

    static class AldorPsiBuilderAdapter extends PsiBuilderAdapter {

        public AldorPsiBuilderAdapter(PsiBuilder delegate) {
            super(delegate);
        }

    }
    */

    // Return true if last token was a close brace, or looking at a semicolon
    // Used to determine if there is a logical semicolon - ie. statement terminator
    // at the current position.
    @SuppressWarnings("ObjectEquality")
    public static boolean semicolonOrCloseBraceNearby(@NotNull PsiBuilder builder, int level) {
        if (consumeToken(builder, KW_Semicolon)) {
            return true;
        }
        IElementType prevElt = skipDocsAndComments(builder);
        if (prevElt == AldorTokenTypes.KW_CCurly) {
            return true;
        }
        if (builder.eof()) {
            return true;
        }
        return false;
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

    @SuppressWarnings("UnusedParameters")
    public static boolean noRepeatHere(@NotNull PsiBuilder builder, int level) {
        if (Objects.equals(builder.getTokenType(), KW_Repeat)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("UnusedParameters")
    public static boolean backSet(@NotNull PsiBuilder builder, int level) {
        if (consumeToken(builder, KW_BlkNext)) {
            return true;
        }
        return false;
    }


    @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
    static boolean spadAnySym(@NotNull PsiBuilder builder, int level) {
        builder.advanceLexer();
        return true;
    }

    /*
    public static boolean parsePiledExpression(@NotNull  PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "Piled_Expression")) {
            return false;
        }
        if (!nextTokenIs(b, KW_BlkStart)) {
            return false;
        }
        PsiBuilder.Marker m = enter_section_(b);
        boolean r = consumeToken(b, KW_BlkStart);
        r = r && parsePiledContent(b, l);
        r = r && blockEnd(eb);
        exit_section_(b, m, PILED_EXPRESSION, r);
        return r;
    }
*/
    @SuppressWarnings("UnusedParameters")
    public static boolean blockEnd(@NotNull PsiBuilder builder, int l) {
        boolean r = consumeToken(builder, KW_BlkEnd);
        if (!r) {
            r = builder.eof();
        }
        if (!r) {
            IElementType elt = skipDocsAndComments(builder);
            r = (Objects.equals(elt, KW_BlkEnd));
        }

        return r;
    }

    public static boolean parsePiledContent(@NotNull PsiBuilder b, int l, String type) {
        int indentLevel = currentIndentLevel(b);
        //System.out.println("Curr: " + b.getCurrentOffset() + " " + b.getTokenType() + " " + b.getTokenText());
        boolean r = parseOneExpression(b, l + 1, type);
        int c = current_position_(b);
        //noinspection LoopWithImplicitTerminationCondition
        while (true) {
            if (b.eof()) {
                break;
            }
            PsiBuilder.Marker m1 = enter_section_(b);
            boolean r1 = consumeToken(b, KW_BlkNext);
            if (!r1) {
                r1 = checkCurrentIndent(b, indentLevel);
            }
            r1 = r1 && parseOneExpression(b, l + 1, type);
            exit_section_(b, m1, null, r1);
            if (!r1) {
                break;
            }
            if (!empty_element_parsed_guard_(b, "Piled_Expression", c)) {
                break;
            }
            c = current_position_(b);
        }
        consumeToken(b, KW_BlkNext);
        return r;
    }

    @SuppressWarnings("InnerClassTooDeeplyNested")
    private enum ExprParser {
        Pile {
            @Override
            public boolean parse(@NotNull PsiBuilder b, int l) {
                return AldorParser.Doc_Expression(b, l);
            }
        },
        SpadTopLevel {
            @Override
            public boolean parse(@NotNull PsiBuilder b, int l) {
                return AldorParser.SpadTopLevelExpression(b, l);
            }
        };

        public abstract boolean parse(@NotNull PsiBuilder b, int l);
    }

    private static boolean parseOneExpression(@NotNull PsiBuilder b, int l, String type) {
        return ExprParser.valueOf(type).parse(b, l);
    }


    static boolean checkCurrentIndent(@NotNull PsiBuilder builder, int indentLevel) {
        int currentIndentLevel = currentIndentLevel(builder);
        //System.out.println("Check level: " + builder.getCurrentOffset() + " " + indentLevel + " " + currentIndentLevel);
        return currentIndentLevel == indentLevel;
    }

    public static int currentIndentLevel(@NotNull PsiBuilder builder) {
        AldorIndentLexer lexer = (AldorIndentLexer) ((Builder) builder).getLexer();
        int level = lexer.indentLevel(builder.getCurrentOffset());
        if (level < 0) {
            return 0;
        }
        return level;
    }


    static boolean isSpadMode(@NotNull PsiBuilder builder, int indentLevel) {
        AldorIndentLexer lexer = (AldorIndentLexer) ((Builder) builder).getLexer();
        return lexer.isSpadMode();
    }

    static boolean isAldorMode(@NotNull PsiBuilder builder, int indentLevel) {
        AldorIndentLexer lexer = (AldorIndentLexer) ((Builder) builder).getLexer();
        return lexer.isAldorMode();
    }

}
