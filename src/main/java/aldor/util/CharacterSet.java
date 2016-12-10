package aldor.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;


public final class CharacterSet extends AbstractSet<Character> {
    private final BitSet bits;
    private final int size;

    private CharacterSet(int size, BitSet bits) {
        this.bits = bits;
        this.size = size;
    }

    public static CharacterSet create(Collection<Character> chars) {
        Optional<Character> max = chars.stream().max(Comparator.<Character>naturalOrder().reversed());
        if (!max.isPresent()) {
            return new CharacterSet(0, new BitSet(0));
        }
        else {
            BitSet bits = new BitSet(max.get() + 1);
            int sz = 0;
            for (Character c: chars) {
                if (!bits.get(c)) {
                    sz++;
                    bits.set(c, true);
                }
            }
            return new CharacterSet(sz, bits);
        }
    }

    @NotNull
    @Override
    public Iterator<Character> iterator() {
        //noinspection NumericCastThatLosesPrecision
        return bits.stream().mapToObj(x -> ((char) x)).iterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Character)) {
            return false;
        }
        Character c = (Character) o;
        if (c >= bits.size()) {
            return false;
        }
        return bits.get(c);
    }
}
