package aldor.make;

import org.jetbrains.jps.incremental.messages.CompilerMessage;

public interface GenericOutputParser {
    void newMessage(String text);

    void close();

    public interface Listener {
        void messageReceived(CompilerMessage message);
    }

}
