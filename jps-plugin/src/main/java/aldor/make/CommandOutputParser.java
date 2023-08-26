package aldor.make;

import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

public class CommandOutputParser implements GenericOutputParser {
    private final Listener listener;
    private final String compilerName;

    public CommandOutputParser(String compilerName, Listener listener) {
        this.compilerName = compilerName;
        this.listener = listener;
    }

    @Override
    public void newMessage(String text) {
        listener.messageReceived(new CompilerMessage(this.compilerName, BuildMessage.Kind.INFO, text));
    }

    @Override
    public void close() {

    }
}
