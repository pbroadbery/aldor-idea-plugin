package aldor.make;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aldor.make.CompileOutputParser.State.ErrorBody;
import static aldor.make.CompileOutputParser.State.ErrorLocations;
import static aldor.make.CompileOutputParser.State.NoError;
import static java.util.regex.Pattern.compile;
import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.*;

public class CompileOutputParser {
    private static final Pattern firstLine = compile("\"([^\"]*)\", line (\\d+): $");
    private static final int     firstLine_grp_1_file = 1;
    private static final int     firstLine_grp_2_file = 2;
    private static final Pattern locatorLine = compile("^\\[L(\\d+) C(\\d+)\\] #(\\d+) \\((\\w+)\\) (.*)$");
    private static final int     locatorLine_grp_1_line = 1;
    private static final int     locatorLine_grp_2_column = 2;
    private static final int     locatorLine_grp_3_errNo = 3;
    private static final int     locatorLine_grp_4_kind = 4;
    private static final int     locatorLine_grp_5_msg = 5;
    private final String compilerName;
    private final File baseDirectory;

    @NotNull
    private State state;
    private final Listener listener;
    @Nullable
    private String file;

    CompileOutputParser(String compilerName, File baseDirectory, Listener listener) {
        this.state = NoError;
        this.compilerName = compilerName;
        this.listener = listener;
        this.baseDirectory = baseDirectory;
        this.file = null;
    }

    void newMessage(String text) {
        CompilerMessage message = process(text);
        if (message != null) {
            listener.messageReceived(message);
        }
    }

    private CompilerMessage process(String text) {
        switch (state) {
            case NoError:
                return processNoError(text);
            case ErrorBody:
                return processErrorBody(text);
            case ErrorLocations:
                return processErrorLocations(text);
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    @Nullable
    private CompilerMessage processNoError(CharSequence text) {
        Matcher matcher = firstLine.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        state = ErrorBody;
        file = matcher.group(firstLine_grp_1_file);
        return null;
    }

    @Nullable
    private CompilerMessage processErrorBody(CharSequence text) {
        Matcher matcher = locatorLine.matcher(text);
        if (matcher.matches()) {
            state = ErrorLocations;
            return errorMessageForText(text, matcher);
        }
        return null;
    }

    @Nullable
    private CompilerMessage processErrorLocations(String text) {
        Matcher matcher = locatorLine.matcher(text);
        if (matcher.matches()) {
            state = ErrorLocations;
            return errorMessageForText(text, matcher);
        }
        else {
            this.file = null;
            this.state = NoError;
            return null;
        }
    }


    private CompilerMessage errorMessageForText(CharSequence text, MatchResult matcher) {
        String lineNumberText = matcher.group(locatorLine_grp_1_line);
        String columnNumberText = matcher.group(locatorLine_grp_2_column);
        String messageNumberText = matcher.group(locatorLine_grp_3_errNo);
        String messageKindText = matcher.group(locatorLine_grp_4_kind);
        String errorMessageText = matcher.group(locatorLine_grp_5_msg);
        BuildMessage.Kind kind = AldorErrorKind.valueOf(messageKindText).buildMessageKind;

        File file = new File(baseDirectory, this.file);

        return new CompilerMessage(compilerName,
                                    kind, errorMessageText,
                                    file.getAbsolutePath(), -1L, -1L, -1L,
                                    Integer.parseInt(lineNumberText),
                                    Integer.parseInt(columnNumberText));
    }

    public interface Listener {
        void messageReceived(CompilerMessage message);
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    enum State {
        NoError, ErrorBody, ErrorLocations;
    }

    enum AldorErrorKind {
        Error(ERROR), Warning(WARNING), Note(INFO) ;

        private final BuildMessage.Kind buildMessageKind;

        AldorErrorKind(BuildMessage.Kind kind) {
            this.buildMessageKind = kind;
        }
    }


}
