package aldor.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Functions {

    public <T> Function<Object, Optional<T>> as(Class<T> clss) {
        return obj -> {
            if (clss.isAssignableFrom(obj.getClass())) {
                return Optional.of(clss.cast(obj));
            }
            return Optional.empty();
        };
    }

    public static <T, V> Predicate<T> compose(Function<T, V> fn, Predicate<V> pred) {
        return t -> pred.test(fn.apply(t));
    }


}
