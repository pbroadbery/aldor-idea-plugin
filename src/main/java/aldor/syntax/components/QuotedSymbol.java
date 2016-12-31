package aldor.syntax.components;

import aldor.psi.AldorQuoteExpr;

import java.util.Collections;

public class QuotedSymbol extends SyntaxNode<AldorQuoteExpr> {

    public QuotedSymbol(AldorQuoteExpr ids) {
        super(ids, Collections.emptyList());
    }

    @Override
    public String name() {
        return "QuotedSymbol";
    }

    @Override
    public String toString() {
        return "(Quote " + psiElement().getQuotedSym().getText() + ")";
    }

}
