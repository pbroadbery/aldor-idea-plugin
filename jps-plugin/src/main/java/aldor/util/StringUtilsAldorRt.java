package aldor.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StringUtilsAldorRt {

    private StringUtilsAldorRt() {}

    @NotNull
    @Contract(pure = true)
    public static String trimExtension(@NotNull String name) {
        int index = name.lastIndexOf('.');
        return (index < 0) ? name : name.substring(0, index);
    }
}
