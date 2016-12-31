package aldor.util.sexpr.impl;

import aldor.util.Iterators;
import aldor.util.sexpr.SExpression;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSequentialList;
import java.util.ListIterator;

/**
 * Just enough to turn SExpression into a list.
 * Don't expect it to be efficient, especially with random access queries.
 *
 * @author pab
 */
final class SExpressionList extends AbstractSequentialList<SExpression> {
    private int size = -1;
    private final SExpression sx;

    SExpressionList(SExpression sx) {
        this.sx = sx;
    }

    @NotNull
    @Override
    public ListIterator<SExpression> listIterator(int index) {
        SExpression startSx = sx;
        for (int i = 0; i < index; i++) {
            startSx = startSx.cdr();
        }
        return Iterators.listIterator(new SExpressionIterator(startSx));
    }

    @Override
    public int size() {
        if (size == -1) {
            int count = 0;
            for (@SuppressWarnings("unused") SExpression elt : this) {
                count++;
            }
            size = count;
        }
        return size;
    }
}
