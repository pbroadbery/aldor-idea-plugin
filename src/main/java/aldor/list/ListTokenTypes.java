package aldor.list;

import com.google.common.collect.Maps;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Map;

public final class ListTokenTypes {
    static final Map<String, ListTokenType> tokenTypeForString = Maps.newHashMap();
    static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    static final IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

    static final IElementType END_OF_LINE_COMMENT = new ListElementType("END_OF_LINE_COMMENT");
    static final IElementType KEY_CHARACTERS = new ListElementType("KEY_CHARACTERS");
    static final IElementType VALUE_CHARACTERS = new ListElementType("VALUE_CHARACTERS");
    static final IElementType KEY_VALUE_SEPARATOR = new ListElementType("KEY_VALUE_SEPARATOR");

    static final ListTokenType OP = createToken("OP");
    static final ListTokenType CP = createToken("CP");
    static final ListTokenType OSQ = createToken("OSQ");
    static final ListTokenType CSQ = createToken("CSQ");

    static final ListTokenType V1 = createToken("V1");
    static final ListTokenType V1a = createToken("V1a");
    static final ListTokenType V2 = createToken("V2");
    static final ListTokenType E = createToken("E");
    static final ListTokenType Sep = createToken("Sep");

    static final TokenSet COMMENTS = TokenSet.create(END_OF_LINE_COMMENT);
    static final TokenSet WHITESPACES = TokenSet.create(WHITE_SPACE);

    public static ListTokenType createToken(String token) {
        if (tokenTypeForString.containsKey(token)) {
            return tokenTypeForString.get(token);
        }
        tokenTypeForString.put(token, new ListTokenType(token));
        return tokenTypeForString.get(token);
    }
}