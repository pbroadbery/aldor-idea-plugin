package aldor.util;

import java.util.Optional;
import java.util.function.Function;

public class Functions {

    public <T> Function<Object, Optional<T>> as(Class<T> clss) {
        return obj -> {
            if (clss.isAssignableFrom(obj.getClass())) {
                return Optional.of(clss.cast(obj));
            }
            return Optional.empty();
        };
    }

}
