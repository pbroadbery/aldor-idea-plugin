package aldor.make;

import aldor.util.Joiners;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aldor.make.CompileOutputParser.State.ErrorBody;
import static aldor.make.CompileOutputParser.State.ErrorLocations;
import static aldor.make.CompileOutputParser.State.NoError;
import static java.util.regex.Pattern.compile;

public class CompileOutputParser {
    private static final Pattern firstLine = compile("\"([^\"]*)\", line (\\d+): .*$");
    private static final int     firstLine_grp_1_file = 1;
    private static final int     firstLine_grp_2_file = 2;
    private static final Pattern locatorLine = compile("^\\[L(\\d+) C(\\d+)] #(\\d+) \\((\\w+)\\) (.*)$");
    private static final int     locatorLine_grp_1_line = 1;
    private static final int     locatorLine_grp_2_column = 2;
    private static final int     locatorLine_grp_3_errNo = 3;
    private static final int     locatorLine_grp_4_kind = 4;
    private static final int     locatorLine_grp_5_msg = 5;
    public static final int MAX_ERROR_MESSAGE_LINES = 20;
    private final String compilerName;
    private final File baseDirectory;

    @NotNull
    private State state;
    private List<String> messageBody;

    private final Listener listener;
    @Nullable
    private String file;

    CompileOutputParser(String compilerName, File baseDirectory, Listener listener) {
        this.state = NoError;
        this.compilerName = compilerName;
        this.listener = listener;
        this.baseDirectory = baseDirectory;
        this.file = null;
        this.messageBody = new ArrayList<>();
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

    void close() {
        CompilerMessage message = this.errorMessageForText(messageBody);
        if (message != null) {
            this.listener.messageReceived(message);
        }
    }

    @SuppressWarnings("SameReturnValue")
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
    private CompilerMessage processErrorBody(String text) {
        Matcher matcher = locatorLine.matcher(text);
        if (matcher.matches()) {
            state = ErrorLocations;
            return addErrorMessage(text);
        }
        return null;
    }

    @Nullable
    private CompilerMessage processErrorLocations(String text) {
        Matcher matcher = locatorLine.matcher(text);
        if (matcher.matches()) {
            state = ErrorLocations;
            return addErrorMessage(text);
        }
        else if (firstLine.matcher(text).matches()) {
            CompilerMessage message = errorMessageForText(this.messageBody);
            messageBody = new ArrayList<>();
            this.processNoError(text);
            return message;
        }
        else if (!text.isEmpty() && Character.isWhitespace(text.charAt(0))) {
            this.state = ErrorLocations;
            messageBody.add(text);
            return null;
        }
        else if (!this.messageBody.isEmpty()){
            CompilerMessage message = errorMessageForText(this.messageBody);
            this.messageBody = new ArrayList<>();
            return message;
        }
        else {
            return null;
        }
    }

    @Nullable
    CompilerMessage addErrorMessage(String text) {
        CompilerMessage message = errorMessageForText(messageBody);
        messageBody = new ArrayList<>();
        messageBody.add(text);
        return message;
    }

    @Nullable
    private CompilerMessage errorMessageForText(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        Matcher matcher = locatorLine.matcher(lines.get(0));
        if (!matcher.matches()) {
            return new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, "ERR: " +
                    Joiners.truncate(MAX_ERROR_MESSAGE_LINES, lines));
        }
        String body = Joiners.truncate(MAX_ERROR_MESSAGE_LINES, lines.subList(1, lines.size()));
        String lineNumberText = matcher.group(locatorLine_grp_1_line);
        String columnNumberText = matcher.group(locatorLine_grp_2_column);
        //String messageNumberText = matcher.group(locatorLine_grp_3_errNo);
        String messageKindText = matcher.group(locatorLine_grp_4_kind);
        String errorMessageText = matcher.group(locatorLine_grp_5_msg);
        BuildMessage.Kind kind = AldorErrorKind.valueOf(messageKindText).buildMessageKind();

        errorMessageText = errorMessageText + (body.isEmpty() ? "": "\n") + body;
        if (file == null) {
            return new CompilerMessage(compilerName, kind, errorMessageText);
        } else {
            return new CompilerMessage(compilerName,
                    kind, errorMessageText,
                    new File(baseDirectory, this.file).getAbsolutePath(),
                    -1L, -1L, -1L,
                    Integer.parseInt(lineNumberText),
                    Integer.parseInt(columnNumberText));

        }
    }
    public interface Listener {
        void messageReceived(CompilerMessage message);
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    enum State {
        NoError, ErrorBody, ErrorLocations
    }


}
