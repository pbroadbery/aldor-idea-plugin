package aldor.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class Joiners {

    public static String truncate(int maxLines, List<String> lines) {
        return lines.subList(0, Math.min(maxLines, lines.size())).stream().collect(elidingCollector(maxLines, lineCombiningCollector()));
    }

    @NotNull
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "NullableProblems"})
    private static <T> Collector<String, ?, String> elidingCollector(int maxEntries, @NotNull Collector<String, T, String> collector) {
        Supplier<Accumulator<T>> supplier = () -> new Accumulator<>(collector.supplier().get());
        BiConsumer<Accumulator<T>, String> accumulator = (acc, s) -> {
            if (acc.count < maxEntries) {
                collector.accumulator().accept(acc.acc, s);
                acc.count++;
            }
        };
        BinaryOperator<Accumulator<T>> combiner = (a, b) -> {throw new UnsupportedOperationException("nope");};
        Function<Accumulator<T>, String> finisher = acc -> {
            if (acc.count >= maxEntries) {
                collector.accumulator().accept(acc.acc, "...");
            }
            return collector.finisher().apply(acc.acc);
        };
        return Collector.of(supplier, accumulator, combiner, finisher);

    }

    private static class Accumulator<X> {
        private final X acc;
        private int count;

        Accumulator(X acc) {
            this.acc = acc;
            this.count = 0;
        }
    }

    @NotNull
    private static Collector<String,StringBuilder,String> lineCombiningCollector() {
        Supplier<StringBuilder> supplier = StringBuilder::new;
        BiConsumer<StringBuilder, String> accumulator = (b, s) -> {
            if (b.length() != 0) {
                b.append('\n');
            }
            b.append(s);};
        BinaryOperator<StringBuilder> combiner = StringBuilder::append;
        Function<StringBuilder, String> finisher = StringBuilder::toString;
        return Collector.of(supplier, accumulator, combiner, finisher);
    }


}
