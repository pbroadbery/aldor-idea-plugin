package aldor.lexer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * Fun facts about system commands - #foo
 */
@SuppressWarnings("ClassNamingConvention")
public class SysCmd {

    public enum SysCommandType {
        Pile("pile"), EndPile("endpile"), Include("include"), Library("library"), If("if"), EndIf("endif"), Invalid("");

        private final String cmdName;

        SysCommandType(String cmdName) {
            this.cmdName = cmdName;
        }

        @Nullable
        public static SysCommandType lookup(@NotNull String name) {
            for (SysCommandType type: values()) {
                if (type.cmdName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public String cmdName() {
            return cmdName;
        }
    }

    private final SysCommandType type;
    private final String[] text;

    @NotNull
    public static SysCmd parse(@NotNull String text) {
        if ((text.charAt(0) != '#') && (text.charAt(0) != ')')) {
            throw new IllegalArgumentException("Text must start with '#' or ')'" + text);
        }
        String[] words = text.substring(1).split(" ");
        SysCommandType type = SysCommandType.lookup(words[0]);
        if (type == null) {
            return new SysCmd(SysCommandType.Invalid, words);
        }
        return new SysCmd(type, words);
    }


    SysCmd(SysCommandType type, String... text) {
        this.type = type;
        this.text = text;
    }

    public Collection<String> text() {
        return Arrays.asList(text);
    }

    public SysCommandType type() {
        return type;
    }

}
