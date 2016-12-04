package aldor.make;

import org.jetbrains.jps.incremental.messages.BuildMessage;

import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.ERROR;
import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.INFO;
import static org.jetbrains.jps.incremental.messages.BuildMessage.Kind.WARNING;

public enum AldorErrorKind {
    Error(ERROR), Warning(WARNING), Note(INFO);

    private final BuildMessage.Kind buildMessageKind;

    AldorErrorKind(BuildMessage.Kind kind) {
        this.buildMessageKind = kind;
    }

    public BuildMessage.Kind buildMessageKind() {
        return buildMessageKind;
    }
}
