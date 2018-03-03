package aldor.util;

import org.jetbrains.annotations.NotNull;

public final class Assertions {

    @NotNull
    public static <T> T isNotNull(T item) {
        assert item != null;
        return item;
    }
}
