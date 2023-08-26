package aldor.util;

public interface UnsafeSupplier<T> {
    T get() throws Exception;
}
