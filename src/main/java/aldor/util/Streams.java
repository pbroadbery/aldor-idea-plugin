package aldor.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Streams {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> toStream(Optional<T> opt) {
        return opt.map(Collections::singletonList).orElse(Collections.emptyList()).stream();
    }

    public static <T> Stream<T> toStream(@Nullable T opt) {
        Collection<T> coll = (opt == null) ? Collections.emptyList() : Collections.singleton(opt);
        return coll.stream();
    }

    public static <A, B, C> Stream<C> zip(BaseStream<A, Stream<A>> streamA, BaseStream<B, Stream<B>> streamB, BiFunction<A, B, C> fn) {
        Iterator<A> aIter = streamA.iterator();
        Iterator<B> bIter = streamB.iterator();

        Iterator<C> iter = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return aIter.hasNext() && bIter.hasNext();
            }

            @Override
            public C next() {
                return fn.apply(aIter.next(), bIter.next());
            }
        };
        Iterable<C> iterable = () -> iter;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T, E extends T> Function<T, Stream<E>> filterAndCast(Class<E> clss) {
        return elt -> clss.isAssignableFrom(elt.getClass()) ? Stream.of(clss.cast(elt)): Stream.empty();
    }
}
