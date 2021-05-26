package aldor.util;

import aldor.test_util.JUnitsBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.testFramework.TestLoggerFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.junit.rules.TestRule;

import java.util.Objects;

import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.helpers.UtilLoggingLevel.INFO;

public class JUnitsRt {
    public static final TestRule setLogToInfoTestRule = JUnitsBase.prePostTestRule(JUnitsRt::setLogToInfo, LogManager::resetConfiguration);
    public static final TestRule setLogToDebugTestRule = JUnitsBase.prePostTestRule(JUnitsRt::setLogToDebug, LogManager::resetConfiguration);
    private static final Logger LOG = Logger.getInstance(JUnitsRt.class);

    public static Runnable setLogToDebug() {
        return isCIBuild() ? () -> {} : setLogLevel(DEBUG);
    }

    public static Runnable setLogToInfo() {
        return isCIBuild() ? () -> {} : setLogLevel(INFO);
    }

    private static Runnable setLogLevel(Level level) {
        LogManager.resetConfiguration();
        Appender appender = new ConsoleAppender(new PatternLayout("%r [%40t] %p %.40c %x - %m%n"));
        appender.setName("Console");
        LogManager.getRootLogger().addAppender(appender);
        LogManager.getRootLogger().setLevel(level);
        Logger.setFactory(TestLoggerFactory.class);

        return () -> LogManager.getRootLogger().removeAppender(appender);
    }

    public static boolean isCIBuild() {
        //noinspection AccessOfSystemProperties
        return Objects.equals(System.getProperty("aldor.build.skip_ci"), "true");
    }

}
