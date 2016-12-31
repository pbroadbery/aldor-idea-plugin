package aldor.util.sexpr.impl;

import aldor.util.sexpr.SExpression;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class SExpressionIterator implements Iterator<SExpression> {
    private SExpression sx;

    SExpressionIterator(SExpression sx) {
        this.sx = sx;
    }

    @Override
    public boolean hasNext() {
        return !sx.isNull();
    }

    @Override
    public SExpression next() {
        if (sx.isNull()) {
            throw new NoSuchElementException("SExpression: next on nil");
        }
        SExpression item = sx.car();
        sx = sx.cdr();
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("nope");
    }

}
