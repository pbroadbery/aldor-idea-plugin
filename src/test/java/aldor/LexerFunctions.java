package aldor;

import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import com.google.common.collect.Lists;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 */
public final class LexerFunctions {


    public static List<IElementType> readTokens(Lexer lla) {
        List<IElementType> tokens = Lists.newArrayList();
        while (lla.getTokenType() != null) {
            tokens.add(lla.getTokenType());
            lla.advance();
        }
        return tokens;
    }

    public static NavigableMap<Integer, IElementType> tokens(CharSequence text) {
        AldorIndentLexer lexer = new AldorIndentLexer(new AldorLexerAdapter());

        lexer.start(text);
        NavigableMap<Integer, IElementType> map = new TreeMap<>();
        while (lexer.getTokenType() != null) {
            map.put(lexer.getTokenStart(), lexer.getTokenType());
            lexer.advance();
        }
        return map;
    }
}
