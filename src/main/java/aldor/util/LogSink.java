package aldor.util;

import com.intellij.openapi.diagnostic.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class LogSink {
    private final Logger LOG = Logger.getInstance(LogSink.class);
    private final Map<String, Object> lastForKey = new ConcurrentHashMap<>();
    private final Class<?> clzz;

    public LogSink(Class<?> clzz) {
        this.clzz = clzz;
    }

    public void info(String key, Object id, String text) {
        if (!Objects.equals(lastForKey.get(key), id)) {
            LOG.info(clzz.getCanonicalName() + ": " + text);
            lastForKey.put(key, id);
        }
    }

}
