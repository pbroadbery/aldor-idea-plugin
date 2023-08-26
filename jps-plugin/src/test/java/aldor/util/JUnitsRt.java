package aldor.util;

import aldor.test_util.JUnitsBase;
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.rules.TestRule;

import java.util.Objects;

public class JUnitsRt {
    public static final TestRule setLogToInfoTestRule = JUnitsBase.prePostTestRule(JUnitsRt::setLogToInfo, JUnitsRt::resetLog);
    public static final TestRule setLogToDebugTestRule = JUnitsBase.prePostTestRule(JUnitsRt::setLogToDebug, JUnitsRt::resetLog);

    private static void resetLog() {
    }

    private static final Logger LOG = Logger.getInstance(JUnitsRt.class);

    public static Runnable setLogToDebug() {
        return isCIBuild() ? () -> {} : setLogLevel(LogLevel.DEBUG);
    }

    public static Runnable setLogToInfo() {
        return isCIBuild() ? () -> {} : setLogLevel(LogLevel.INFO);
    }

    private static Runnable setLogLevel(LogLevel level) {
        return () -> {};
    }

    public static boolean isCIBuild() {
        //noinspection AccessOfSystemProperties
        return Objects.equals(System.getProperty("aldor.build.skip_ci"), "true");
    }

}
