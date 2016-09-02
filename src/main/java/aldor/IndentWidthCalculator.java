package aldor;

/**
 * Calculates tab widths; ultimately, TAB_WIDTH should come from the project.
 */
public class IndentWidthCalculator {
    private static final int TAB_WIDTH = 8;

    public int width(CharSequence seq) {
        int width=0;

        for (int i=0; i<seq.length(); i++) {
            if (seq.charAt(i) == ' ') {
                width++;
            }
            else if (seq.charAt(i) == '\t') {
                width = width + (TAB_WIDTH - (width % TAB_WIDTH));
            }

        }
        return width;
    }

}
