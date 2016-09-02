package pab.aldor;

import com.google.common.collect.Lists;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

import java.util.List;

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
}
