package aldor.syntax;

@FunctionalInterface
public interface GuardedRunnable<E extends Exception> {

    @SuppressWarnings("ProhibitedExceptionDeclared")
    void callInternal() throws E;

    default void call() {
        //noinspection OverlyBroadCatchBlock
        try {
            callInternal();
        } catch (Exception e) {
            throw new GuardedException(e);
        }
    }

    static <E extends Exception> void gaurd(Class<E> exn, GuardedRunnable<E> runnable) {
        //noinspection OverlyBroadCatchBlock
        try {
            runnable.callInternal();
        } catch (Exception e) {
            if (exn.isAssignableFrom(e.getClass())) {
                throw new GuardedException(e);
            }
            else {
                throw new IllegalStateException(e);
            }
        }
    }
}
