package aldor.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class InstanceCounter {
    private final Map<Class<?>, AtomicInteger> countForClass = new ConcurrentHashMap<>();
    private static final InstanceCounter instance = new InstanceCounter();

    private InstanceCounter() {}

    public static InstanceCounter instance() {
        return instance;
    }

    public int next(Class<?> clss) {
        AtomicInteger counter = countForClass.computeIfAbsent(clss, cl -> new AtomicInteger(0));
        return counter.getAndIncrement();
    }
}
