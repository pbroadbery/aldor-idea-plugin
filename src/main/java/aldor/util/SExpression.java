package aldor.util;

import aldor.util.sexpr.SExpressionReader;
import aldor.util.sexpr.SExpressionTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static aldor.util.SymbolPolicy.NORMAL;

public abstract class SExpression {
    private final SxType<?> type;

    protected SExpression(SxType<?> type) {
        this.type = type;
    }

    public SxType<?> type() {
        return type;
    }

    public static SExpression read(Reader reader, SymbolPolicy symbolPolicy) {
        SExpressionReader rdr = new SExpressionReader(reader, symbolPolicy);
        return rdr.read();
    }

    public static SExpression read(Reader reader) {
        return read(reader, NORMAL);
    }

    public <T extends SExpression> T asType(SxType<T> type) {
        return type.cast(this);
    }

    @Override
    public String toString() {
        Writer sw = new StringWriter();
        try {
            write(sw);
        } catch (IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    @Override
    public final boolean equals(Object otherObj) {
        if (!(SExpression.class.isAssignableFrom(otherObj.getClass()))) {
            return false;
        }

        SExpression other = (SExpression) otherObj;
        if (this.type() != other.type()) {
            return false;
        }

        return innerEqual(other);
    }

    @Override
    public int hashCode() {
        return hashCode(0);
    }

    public abstract int hashCode(int level);

    protected abstract boolean innerEqual(SExpression other);

    public static SExpression cons(@NotNull SExpression car, @NotNull SExpression cdr) {
        return new SExpressionTypes.Cons(car, cdr);
    }

    public int integer() {
        throw typeConversionException(SxType.Integer);
    }

    @SuppressWarnings("QuestionableName")
    public String string() {
        throw typeConversionException(SxType.String);
    }

    public String symbol() {
        throw typeConversionException(SxType.Symbol);
    }

    public static SExpression integer(@NotNull Integer value) {
        return new SExpressionTypes.IntegerAtom(value);
    }

    @SuppressWarnings("QuestionableName")
    public static SExpression string(@NotNull String substring) {
        return new SExpressionTypes.StringAtom(substring);
    }

    public static SExpression symbol(@NotNull String text) {
        return new SExpressionTypes.SymbolAtom(text);
    }

    public SExpression car() {
        throw typeConversionException(SxType.Cons);
    }

    public SExpression cdr() {
        throw typeConversionException(SxType.Cons);
    }

    public static SExpression nil() {
        return SExpressionTypes.nil();
    }


    public void setCar(SExpression car) {
        throw typeConversionException(SxType.Cons);
    }

    public void setCdr(SExpression cdr) {
        throw typeConversionException(SxType.Cons);
    }

    public abstract void write(Writer w) throws IOException;

    public boolean isOfType(SxType<?> type) {
        return Objects.equals(this.type, type);
    }

    public boolean isNull() {
        return isOfType(SxType.Nil);
    }

    public List<SExpression> asList() {
        return new SExpressionList(this);
    }

    public Map<SExpression, SExpression> asAssociationList() {
        return new AssociationList(this);
    }

    public SExpression nth(int i) {
        int countdown = i;
        SExpression result = this;
        if (countdown < 0) {
            throw new IllegalArgumentException("i must be non-negative");
        }
        while (countdown > 0) {
            countdown = countdown - 1;
            result = result.cdr();
        }
        return result.car();
    }

    /**
     * Just enough to turn SExpression into a list.
     * Don't expect it to be efficient, especially with random access queries.
     *
     * @author pab
     */
    private static final class SExpressionList extends AbstractSequentialList<SExpression> {
        private int size = -1;
        private final SExpression sx;

        SExpressionList(SExpression sx) {
            this.sx = sx;
        }

        @NotNull
        @Override
        public ListIterator<SExpression> listIterator(int index) {
            SExpression startSx = sx;
            for (int i = 0; i < index; i++) {
                startSx = startSx.cdr();
            }
            return Iterators.listIterator(new SExpressionIterator(startSx));
        }

        @Override
        public int size() {
            if (size == -1) {
                int count = 0;
                for (@SuppressWarnings("unused") SExpression elt : this) {
                    count++;
                }
                size = count;
            }
            return size;
        }
    }

    private static final class SExpressionIterator implements Iterator<SExpression> {
        private SExpression sx;

        SExpressionIterator(SExpression sx) {
            this.sx = sx;
        }

        @Override
        public boolean hasNext() {
            return !sx.isNull();
        }

        @Override
        public SExpression next() {
            if (sx.isNull()) {
                throw new NoSuchElementException("SExpression: next on nil");
            }
            SExpression item = sx.car();
            sx = sx.cdr();
            return item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("nope");
        }

    }

    UnsupportedOperationException typeConversionException(SxType<?> expected) {
        return new UnsupportedOperationException("Expected type: " + expected + " found: " + this.type().name());
    }

}
