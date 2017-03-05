package aldor.test_util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Htmls {
    private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

    public static String removeTags(String markupText) {
        if ((markupText == null) || markupText.isEmpty()) {
            return markupText;
        }

        Matcher m = REMOVE_TAGS.matcher(markupText);
        return m.replaceAll("");
    }
}
