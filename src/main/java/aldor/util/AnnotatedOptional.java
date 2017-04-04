package aldor.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AnnotatedOptional<T, X> {
    private final T value;
    private final X failInfo;

    private AnnotatedOptional(T value, X value1) {
        this.failInfo = value1;
        this.value = value;
    }

    public static <T, X> AnnotatedOptional<T, X> of(T t) {
        return new AnnotatedOptional<>(t, null);
    }

    public <S> AnnotatedOptional<S, X> map(Function<T, S> fn) {
        if (value == null) {
            return new AnnotatedOptional<>(null, failInfo);
        }
        else {
            return new AnnotatedOptional<>(fn.apply(value), failInfo);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T1, X1> AnnotatedOptional<T1, X1> fromOptional(Optional<T1> optional, Supplier<X1> failInfo) {
        Optional<AnnotatedOptional<T1, X1>> annotatedMaybe = optional.map(opt -> of(optional.get()));
        return annotatedMaybe.orElse(missing(failInfo.get()));
    }

    public static <T, X> AnnotatedOptional<T, X> ofNullable(T value, Supplier<X> xSupplier) {
        if (value == null) {
            return new AnnotatedOptional<>(null, xSupplier.get());
        }
        else {
            return new AnnotatedOptional<>(value, null);
        }
   }

   public <S> AnnotatedOptional<S, X> flatMap(Function<T, AnnotatedOptional<S, X>> fn) {
       if (value == null) {
           return new AnnotatedOptional<>(null, failInfo);
       }
       else {
           return fn.apply(value);
       }
   }

    public X failInfo() {
        return this.failInfo;
    }

    public boolean isPresent() {
        return value != null;
    }

    public static <T, X> AnnotatedOptional<T, X> missing(X s) {
        return new AnnotatedOptional<>(null, s);
    }

    public T get() {
        return value;
    }

    public T orElseThrow(Function<X, RuntimeException> exceptionSupplier) {
        if (isPresent()) {
            return get();
        }
        //noinspection ProhibitedExceptionThrown
        throw exceptionSupplier.apply(failInfo());
    }

    public T orElseThrowError(Function<X, Error> errorSupplier) {
        if (isPresent()) {
            return get();
        }
        //noinspection ProhibitedExceptionThrown
        throw errorSupplier.apply(failInfo());
    }

    public T orElse(Function<X, T> valueSupplier) {
        if (isPresent()) {
            return get();
        }
        return valueSupplier.apply(this.failInfo);
    }

    @Override
    public String toString() {
        return "{" + (this.value == null ? "FAIL: " + failInfo : "OK: " + value) + "}";
    }

    @Override
    public int hashCode() {
        return this.map(Object::hashCode).orElse(Object::hashCode);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnnotatedOptional)) {
            return false;
        }
        //noinspection unchecked
        AnnotatedOptional<T, X> otherOptional = (AnnotatedOptional<T, X>) other;
        if (isPresent() && otherOptional.isPresent()) {
            return Objects.equals(this.get(), otherOptional.get());
        }
        if (!isPresent() && !otherOptional.isPresent()) {
            return Objects.equals(this.failInfo(), otherOptional.failInfo());
        }
        return false;
    }

    public T orElseConstant(T c) {
        return isPresent() ? get() : c;
    }
}
