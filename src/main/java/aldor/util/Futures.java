package aldor.util;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Futures {

    public static <T> T dispatchThreadWaitForFuture(Future<T> fut) throws ExecutionException {
        while (true) {
            try {
                var val = fut.get(100, TimeUnit.MILLISECONDS);
                return val;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (TimeoutException e) {
            }
            ProgressManager.checkCanceled();
        }
    }

}
