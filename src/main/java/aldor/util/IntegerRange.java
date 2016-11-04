package aldor.util;

import java.util.AbstractList;

public class IntegerRange extends AbstractList<Integer> {
    private final int low;
    private final int high;

    public IntegerRange(int low, int high) {
        if (high < low) {
            throw new IllegalArgumentException("range out of order");
        }
        this.low = low;
        this.high = high;
    }

    @Override
    public Integer get(int i) {
        if ((i < 0) || (i >= size())) {
            throw new IndexOutOfBoundsException("Out of bounds");
        }
        return low + i;
    }

    @Override
    public int size() {
        return high - low;
    }
}
