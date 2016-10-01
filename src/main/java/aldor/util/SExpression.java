package aldor.util;

import aldor.util.sexpr.SExpressionReader;
import aldor.util.sexpr.SExpressionTypes;
import aldor.util.sexpr.SxType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

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

	public static SExpression cons(SExpression car, SExpression cdr) {
		return new SExpressionTypes.Cons(car, cdr);
	}

	public int integer() {
		throw new UnsupportedOperationException();
	}

	public String string() {
		throw new UnsupportedOperationException("string: " + this);
	}

	public String symbol() {
		throw new UnsupportedOperationException(this.toString());
	}

	public static SExpression integer(Integer value) {
		return new SExpressionTypes.IntegerAtom(value);
	}

	public static SExpression string(String substring) {
		return new SExpressionTypes.StringAtom(substring);
	}

	public static SExpression symbol(String text) {
		return new SExpressionTypes.SymbolAtom(text);
	}

	public SExpression car() {
		throw new UnsupportedOperationException("car: "+this);
	}

	public SExpression cdr() {
		throw new UnsupportedOperationException();
	}

	public static SExpression nil() {
		return SExpressionTypes.nil();
	}


	public void setCar(SExpression car) {
		throw new UnsupportedOperationException();
	}

	public void setCdr(SExpression cdr) {
		throw new UnsupportedOperationException();
	}

	public void write(Writer w) throws IOException {
		throw new UnsupportedOperationException();
	}

	public boolean isOfType(SxType<?> type) {
		return this.type == type;
	}

	public boolean isNull() {
		return isOfType(SxType.Nil);
	}

	public List<SExpression> asList() {
		return new SExpressionList(this);
	}

	/**
	 * Just enough to turn SExpression into a list.
	 * Don't expect it to be efficient, especially with random access queries.
	 * @author pab
	 *
	 */
	private static final class SExpressionList extends AbstractSequentialList<SExpression> {
		int size = -1;
		final SExpression sx;

		SExpressionList(SExpression sx) {
			this.sx = sx;
		}

		@NotNull
        @Override
		public ListIterator<SExpression> listIterator(int index) {
			SExpression startSx = sx;
			for (int i=0; i<index; i++) {
				startSx = startSx.cdr();
			}
			return Iterators.listIterator(new SExpressionIterator(startSx));
		}

		@Override
		public int size() {
			if (size == -1) {
				int count = 0;
				for (@SuppressWarnings("unused") SExpression elt: this) {
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
                throw new NoSuchElementException();
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

}
