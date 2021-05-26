package aldor.test_util;

/**
 * For those odd times when you want a runnable that could
 * do anything.
 */
@FunctionalInterface
public interface UnsafeRunnable {
    void run() throws Exception;
}
