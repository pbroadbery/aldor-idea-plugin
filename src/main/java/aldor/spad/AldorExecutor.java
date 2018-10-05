package aldor.spad;

import com.intellij.openapi.util.ThrowableComputable;
import foamj.Clos;
import org.jetbrains.annotations.NotNull;

public interface AldorExecutor {
    void run(Runnable r) throws InterruptedException;

    <T, E extends Throwable> T compute(@NotNull ThrowableComputable<T, E> action) throws E, InterruptedException;

    Clos createLoadFn(String className);
}
