package aldor.psi.elements;

import com.google.common.collect.Maps;
import com.intellij.openapi.util.Pair;

import java.util.Map;

public class AldorDefineInfo {
    static final Map<Pair<Level, Classification>, AldorDefineInfo> infoForDetail;
    private final Level level;
    private final Classification classification;

    public AldorDefineInfo(Level level, Classification classification) {
        this.level = level;
        this.classification = classification;
    }

    public Level level() {
        return level;
    }

    public Classification classification() {
        return classification;
    }

    public enum Level { TOP, INNER }
    public enum Classification { DOMAIN, CATEGORY, MACRO, OTHER }

    static {
        infoForDetail = Maps.newHashMap();
        for (Level level: Level.values()) {
            for (Classification classification: Classification.values()) {
                infoForDetail.put(Pair.create(level, classification), new AldorDefineInfo(level, classification));
            }
        }
    }

    public static AldorDefineInfo info(Level level, Classification classification) {
        return infoForDetail.get(Pair.create(level, classification));
    }
}
