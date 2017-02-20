package aldor.util.sexpr.impl;

import aldor.util.Strings;
import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SymbolPolicy;

import java.io.Reader;

public class SExpressionReader {
    private final ITokeniser tokeniser;

    public SExpressionReader(Reader reader, SymbolPolicy symbolPolicy) {
        tokeniser = new WhitespaceFilter(new Tokeniser(reader, symbolPolicy));
    }

    public SExpression read() {
        Token tok = tokeniser.peek();
        tokeniser.next();
        switch (tok.type()) {
            case CParen:
                throw new SExpressionReadException("Parse error");
            case Integer:
                assert tok.text() != null;
                return SExpression.integer(Strings.instance().decode(Integer.class, tok.text()));
            case String:
                return SExpression.string(tok.text().substring(1, tok.text().length() - 1));
            case Symbol:
                return SExpression.symbol(tok.text());
            case OParen:
                return readListOrNil();
            case DOT:
                throw new SExpressionReadException("Parse error: " + tok);
            case WS:
            case NL:
            case EOF:
        }
        throw new SExpressionReadException("Unexpected token: " + tok);
    }

    private SExpression readListOrNil() {
        Token next = tokeniser.peek();
        if (next.type() == TokenType.CParen) {
            tokeniser.next();
            return SExpression.nil();
        } else {
            return readList();
        }
    }

    private SExpression readList() {
        SExpression head = SExpression.cons(SExpression.nil(), SExpression.nil());
        SExpression ptr = head;
        boolean done = false;
        while (!done) {
            SExpression next = read();
            ptr.setCar(next);
            Token nextToken = tokeniser.peek();
            if (nextToken.type() == TokenType.DOT) {
                tokeniser.next();
                SExpression endSx = read();
                ptr.setCdr(endSx);
                nextToken = tokeniser.peek();
                if (nextToken.type() != TokenType.CParen) {
                    throw new SExpressionReadException("Parse error " + nextToken);
                }
                tokeniser.next();
                done = true;
            } else if (nextToken.type() == TokenType.CParen) {
                tokeniser.next();
                done = true;
            } else {
                SExpression newConsCell = SExpression.cons(head, SExpression.nil());
                ptr.setCdr(newConsCell);
                ptr = newConsCell;
            }
        }
        return head;
    }

    public boolean hasNext() {
        return tokeniser.hasNext();
    }

}
