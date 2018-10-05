package aldor.spad;

import com.intellij.openapi.util.ThrowableComputable;
import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AldorExecutorComponent implements AldorExecutor {
    private final FoamContext context;
    private final Lock lock = new ReentrantLock();

    public AldorExecutorComponent() {
        this.context = new FoamContext();
    }

    @Override
    public void run(Runnable r) throws InterruptedException {
        boolean hasLock = lock.tryLock(5, TimeUnit.SECONDS);
        if (!hasLock) {
            throw new RuntimeException("Aldor operation not available");
        }
        try {
            FoamHelper.setContext(context);
            r.run();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public <T, E extends Throwable> T compute(@NotNull ThrowableComputable<T, E> action) throws E, InterruptedException {
        boolean hasLock = lock.tryLock(5, TimeUnit.SECONDS);
        if (!hasLock) {
            throw new RuntimeException("Aldor operation not available");
        }
        try {
            FoamHelper.setContext(context);
            return action.compute();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Clos createLoadFn(String className) {
        return context.createLoadFn(className);
    }
}
