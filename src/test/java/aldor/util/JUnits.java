package aldor.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.testFramework.TestLoggerFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

import static org.apache.log4j.Level.DEBUG;

/**
 * Fun things that unit tests can use.
 */
public final class JUnits {

    public static void setLogToDebug() {
        LogManager.resetConfiguration();
        Appender appender = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
        appender.setName("Console");
        LogManager.getRootLogger().addAppender(appender);
        LogManager.getRootLogger().setLevel(DEBUG);
        Logger.setFactory(TestLoggerFactory.class);
    }

}
