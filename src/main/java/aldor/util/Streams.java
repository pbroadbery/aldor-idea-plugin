package aldor.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public final class Streams {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> toStream(Optional<T> opt) {
        return opt.map(Collections::singletonList).orElse(Collections.emptyList()).stream();
    }

    public static <T> Stream<T> toStream(@Nullable T opt) {
        Collection<T> coll = (opt == null) ? Collections.emptyList() : Collections.singleton(opt);
        return coll.stream();
    }



}
