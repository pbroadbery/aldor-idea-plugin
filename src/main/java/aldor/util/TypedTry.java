package aldor.util;

import com.intellij.openapi.options.ConfigurationException;

import java.util.function.Consumer;
import java.util.function.Function;

/** Use subclasses! */
@SuppressWarnings("ProhibitedExceptionThrown")
public class TypedTry<V, E extends Exception> {
    Class<E> clzz = null;
    E exception = null;
    V value = null;

    public TypedTry(Class<E> clzz, E o, V compute) {
        this.clzz = clzz;
        this.exception = o;
        this.value = compute;
    }

    public <E2 extends Exception> V orElseThrow(Function<E, E2> exnSupplier) throws E2{
        if (exception != null)
            throw exnSupplier.apply(exception);
        return value;
    }

    public V orElse(V exnValue) {
        if (exception != null)
            return exnValue;
        return value;
    }

    public <V2> TypedTry<V2, E> map(Function<V, V2> fn) {
        return (exception == null) ? succeed(clzz, fn.apply(value)) : TypedTry.fail(clzz, exception);
    }

    public void onFailure(Consumer<E> consumer) {
        if (exception != null) {
            consumer.accept(exception);
        }
    }


    public interface FailingSupplier<V, E extends Exception> {
        V compute() throws E, ConfigurationException;
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    public static <V, E1 extends Exception> TypedTry<V, E1> of(Class<E1> clzz, FailingSupplier<V, E1> supplier) {
        try {
            return TypedTry.<E1, V>succeed(clzz, supplier.compute());
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception exception) {
            if (clzz.isAssignableFrom(exception.getClass())) {
                return TypedTry.fail(clzz, clzz.cast(exception));
            }
            throw new RuntimeException("Cannot throw " + exception.getClass(), exception);
        }
    }

    private static <E1 extends Exception, V> TypedTry<V,E1> succeed(Class<E1> clzz, V compute) {
        return new TypedTry<>(clzz, null, compute);
    }

    private static <E1 extends Exception, V> TypedTry<V,E1> fail(Class<E1> clzz, E1 cast) {
        return new TypedTry<>(clzz, cast, null);
    }

}
