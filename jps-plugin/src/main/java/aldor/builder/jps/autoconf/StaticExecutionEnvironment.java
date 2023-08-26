package aldor.builder.jps.autoconf;

import aldor.util.InstanceCounter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class StaticExecutionEnvironment {
    private final int instanceId = InstanceCounter.instance().next(StaticExecutionEnvironment.class);
    private final ExecutorService executorService;
    private static final Logger LOG = Logger.getInstance(StaticExecutionEnvironment.class);

    public StaticExecutionEnvironment() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("builder-" + instanceId + "-%d")
                .setUncaughtExceptionHandler(this::executorUncaughtException)
                .build();
        executorService = Executors.newCachedThreadPool(threadFactory);
    }

    public ExecutorService executorService() {
        return executorService;
    }

    private void executorUncaughtException(Thread thread, Throwable throwable) {
        LOG.error("Uncaught exception: "+ thread.getName());
        LOG.error(throwable);
    }
}
