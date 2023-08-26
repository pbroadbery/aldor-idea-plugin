package aldor.symbolfile;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a position in a file - file, line, column (1-based).
 *
 * The filename does not include a path.
 */
public final class SrcPos implements Comparable<SrcPos> {
    private final int lineNumber;
    private final int columnNumber;
    private final String fileName;

    public SrcPos(String fileName, int lineNumber, int columnNumber) {
        assert (lineNumber > 0) && (columnNumber > 0);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SrcPos other)) {
            return false;
        }
        return (this.lineNumber == other.lineNumber()) && (this.columnNumber == other.columnNumber());
    }

    public final String fileName() {
        return fileName;
    }

    public final int lineNumber() {
        return lineNumber;
    }

    public final int columnNumber() {
        return columnNumber;
    }

    @SuppressWarnings("SubtractionInCompareTo")
    @Override
    public int compareTo(@NotNull SrcPos o) {
        if (this.lineNumber != o.lineNumber()) {
            return this.lineNumber - o.lineNumber();
        } else {
            return this.columnNumber - o.columnNumber();
        }
    }

    @Override
    public int hashCode() {
        return (lineNumber << 3) + (31 * columnNumber);
    }

    @Override
    public String toString() {
        return "[" + fileName + ": " + lineNumber() + "," + columnNumber() + "]";
    }
}
