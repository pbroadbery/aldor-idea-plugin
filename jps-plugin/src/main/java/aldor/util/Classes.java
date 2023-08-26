package aldor.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Classes {

    public static <T> Caster<T> caster(Class<T> clzz) {
        return new Caster<T>(clzz);
    }

    public static class Caster<T> {
        private final Class<T> clzz;

        private Caster(Class<T> clzz) {
            this.clzz = clzz;
        }

        public Optional<T> cast(Object t) {
            return (clzz.isInstance(t)) ? Optional.of(clzz.cast(t)): Optional.empty();
        }
    }

    public static <T, E extends T> Function<T, Stream<E>> filterAndCast(Class<E> clss) {
        return elt -> clss.isAssignableFrom(elt.getClass()) ? Stream.of(clss.cast(elt)): Stream.empty();
    }
}
