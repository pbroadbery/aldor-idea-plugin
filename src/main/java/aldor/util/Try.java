package aldor.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Try<T> {
    @Nullable
    private final Throwable e;
    @Nullable
    private final T value;

    private Try(@Nullable T value) {
        this.value = value;
        this.e = null;
    }
    private Try(@NotNull Throwable t) {
        this.e = t;
        this.value = null;
    }

    public static <T> Try<T> failed(Throwable t) {
        return new Try<>(t);
    }

    public static <T> Try<T> success(T value) {
        return new Try<>(value);
    }

    public static <T> Try<T> of(Supplier<T> fn) {
        try {
            return success(fn.get());
        }
        catch (RuntimeException e) {
            return failed(e);
        }
    }

    public static <T> Try<T> ofUnsafe(UnsafeSupplier<T> fn) {
        try {
            return success(fn.get());
        }
        catch (Exception e) {
            return failed(e);
        }
    }

    @SuppressWarnings("ProhibitedExceptionThrown")
    public T orElseThrow(Function<Throwable, RuntimeException> fn) {
        if (isPresent()) {
            return value;
        } else {
            throw fn.apply(e);
        }
    }

    public T orElse(Function<Throwable, T> fn) {
        if (isPresent()) {
            return value;
        } else {
            return fn.apply(e);
        }
    }

    public Try<T> peekError(Consumer<Throwable> consumer) {
        if (!isPresent()) {
            consumer.accept(e);
        }
        return this;
    }

    public boolean isPresent() {
        return e == null;
    }

    public <S> Try<S> map(Function<T, S> fn) {
        if (isPresent()) {
            return success(fn.apply(value));
        }
        else {
            return failed(e);
        }
    }
}
