package aldor.list;

import com.google.common.collect.Maps;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Map;

public class ListTokenTypes {
    static final Map<String, ListTokenType> tokenTypeForString = Maps.newHashMap();
    static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    static IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

    static final IElementType END_OF_LINE_COMMENT = new ListElementType("END_OF_LINE_COMMENT");
    static IElementType KEY_CHARACTERS = new ListElementType("KEY_CHARACTERS");
    static IElementType VALUE_CHARACTERS = new ListElementType("VALUE_CHARACTERS");
    static IElementType KEY_VALUE_SEPARATOR = new ListElementType("KEY_VALUE_SEPARATOR");

    static ListTokenType OP = createToken("OP");
    static ListTokenType CP = createToken("CP");
    static ListTokenType OSQ = createToken("OSQ");
    static ListTokenType CSQ = createToken("CSQ");

    static ListTokenType V1 = createToken("V1");
    static ListTokenType V1a = createToken("V1a");
    static ListTokenType V2 = createToken("V2");
    static ListTokenType E = createToken("E");
    static ListTokenType Sep = createToken("Sep");

    static TokenSet COMMENTS = TokenSet.create(END_OF_LINE_COMMENT);
    static TokenSet WHITESPACES = TokenSet.create(WHITE_SPACE);


    public static ListTokenType createToken(String token) {
        if (tokenTypeForString.containsKey(token))
            return tokenTypeForString.get(token);
        tokenTypeForString.put(token, new ListTokenType(token));
        return tokenTypeForString.get(token);
    }
}