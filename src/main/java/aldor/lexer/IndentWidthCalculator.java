package aldor.lexer;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Calculates tab widths; ultimately, TAB_WIDTH should come from the project.
 */
public class IndentWidthCalculator {
    private static final Logger LOG = Logger.getInstance(IndentWidthCalculator.class);
    private static final int TAB_WIDTH = 8;

    public int width(CharSequence seq) {
        int width=0;

        for (int i=0; i<seq.length(); i++) {
            if (seq.charAt(i) == '\t') {
                width = width + (TAB_WIDTH - (width % TAB_WIDTH));
            }
            else {
                width++;
            }
        }
        return width;
    }

    public int offsetForWidth(CharSequence seq, int desiredWidth) {
        int i = 0;
        int width = 0;
        while (true) {
            if (seq.charAt(i) == '\t') {
                width = width + (TAB_WIDTH - (width % TAB_WIDTH));
            }
            else {
                width++;
            }

            if (width >= desiredWidth) {
                break;
            }
            i++;
        }
        return i;
    }

}
