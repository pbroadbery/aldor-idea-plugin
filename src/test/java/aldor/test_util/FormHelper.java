package aldor.test_util;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public final class FormHelper {

    public static <T> T component(@Nonnull Object tab, Class<T> clss, String fieldName) {
        try {
            Field field = tab.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            //noinspection unchecked
            return (T) field.get(tab);
        } catch (RuntimeException | NoSuchFieldException | IllegalAccessException e) {
            throw new FormHelperException("Failed to read " + fieldName + " class: "+ tab.getClass(), e);
        }
    }
    @SuppressWarnings("serial")
    private static class FormHelperException extends RuntimeException {
        FormHelperException(String s, Exception e) {
            super(s, e);
        }
    }
}
