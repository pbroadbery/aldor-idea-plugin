package aldor.include;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SysCommandSearch {
    private static final Pattern LIBRARY_PATTERN = Pattern.compile("#library");
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("#include");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("[ \t]+\"([^\r\n\"]+)\"");
    private static final Pattern LIB_IDS_PATTERN = Pattern.compile("[ \t]+[A-Za-z_0-9]+[ \t]+\"([^\r\n\"]+)\"");
    private static final Pattern EOL_PATTERN = Pattern.compile("[\r\n]");

    private static final SysCommandSearch instance = new SysCommandSearch();

    public static SysCommandSearch instance() {
        return instance;
    }

    private SysCommandSearch() {}

    public void searchIncludes(CharSequence text, Consumer<CharSequence> inclFileConsumer) {
        Matcher matcher = INCLUDE_PATTERN.matcher(text);
        while (matcher.find()) {
            int inclOffset = matcher.start();
            Matcher eolMatch = EOL_PATTERN.matcher(text.subSequence(inclOffset, text.length()));
            int end = eolMatch.find() ? (inclOffset + eolMatch.start()) : text.length();
            var line = text.subSequence(inclOffset+8, end);
            Matcher fileMatch = FILENAME_PATTERN.matcher(line);
            if (fileMatch.matches()) {
                String file = fileMatch.group(1);
                inclFileConsumer.accept(file);
            }
        }

    }

    public void searchLibraries(CharSequence contentAsText, Consumer<Pair<CharSequence, CharSequence>> libraryConsumer) {
    }
}
