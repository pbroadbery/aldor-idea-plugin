package aldor.make;

import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CompileOutputParserTest {

    @Test
    @Ignore("just here to test output")
    public void testParser() throws IOException {
        List<CompilerMessage> messages = new ArrayList<>();
        CompileOutputParser.Listener listener = messages::add;
        CompileOutputParser parser = new CompileOutputParser("someCompiler", new File("."), listener);

        List<String> ll = Files.readAllLines(new File("/tmp/qq.txt").toPath());

        for (String s: ll) {
            parser.newMessage(s);
        }
        parser.close();
        for (CompilerMessage message: messages) {
            System.out.println("Message: " + message);
        }
        assertFalse(messages.isEmpty());
    }

    @SuppressWarnings("MagicNumber")
    @Test
    public void testErrorMessages() {
        List<CompilerMessage> messages = new ArrayList<>();
        CompileOutputParser.Listener listener = messages::add;
        CompileOutputParser parser = new CompileOutputParser("someCompiler", new File("."), listener);

        String text = "\"syntax.as\", line 41:     map(args: TForm, ret): % == never\n"
               + ".....................^\n"
                + "[L41 C22] #1 (Error) Whups";
        for (String line: text.split("\n")) {
            parser.newMessage(line);
        }
        parser.close();
        assertEquals(1, messages.size());
        CompilerMessage message = messages.get(0);
        assertEquals(41, message.getLine());
        assertEquals(22, message.getColumn());
        assertEquals("Whups", message.getMessageText());
        assertEquals(BuildMessage.Kind.ERROR, message.getKind());
    }

}
