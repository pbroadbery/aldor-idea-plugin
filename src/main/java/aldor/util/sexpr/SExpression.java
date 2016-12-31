package aldor.util.sexpr;

import aldor.util.sexpr.impl.SExpressionReader;
import aldor.util.sexpr.impl.SExpressionTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static aldor.util.sexpr.SymbolPolicy.NORMAL;

@SuppressWarnings("ClassWithTooManyMethods")
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
        return toString(NORMAL);
    }


    public String toString(SymbolPolicy mode) {
        Writer sw = new StringWriter();
        try {
            write(sw, mode);
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

    public abstract void write(Writer w, SymbolPolicy mode) throws IOException;

    public boolean isOfType(SxType<?> type) {
        return Objects.equals(this.type, type);
    }

    public boolean isNull() {
        return isOfType(SxType.Nil);
    }

    public List<SExpression> asList() {
        throw typeConversionException(SxType.Cons);
    }

    public Map<SExpression, SExpression> asAssociationList() {
        throw typeConversionException(SxType.Cons);
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

    UnsupportedOperationException typeConversionException(SxType<?> expected) {
        return new UnsupportedOperationException("Expected type: " + expected + " found: " + this.type().name());
    }

}
