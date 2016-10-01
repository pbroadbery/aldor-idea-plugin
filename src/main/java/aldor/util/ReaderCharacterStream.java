package aldor.util;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharacterStream implements Stream<Character> {
	private Exception exception = null;
	private int c;
	private final Reader reader;

	public ReaderCharacterStream(Reader stream) {
		this.reader = stream;
		c = -2;
	}

	@Override
	public Character peek() {
		if (c == -2) {
			try {
				c = reader.read();
			} catch (IOException e) {
				this.exception = e;
			}
		}
		//noinspection NumericCastThatLosesPrecision
		return (char) c;
	}

	@Override
	public void next() {
		c = -2;
	}

	@Override
	public boolean hasNext() {
		peek();
		return c != -1;
	}

	public boolean isInError() {
		peek();
		return exception != null;
	}

	public void throwError() {
        //noinspection ProhibitedExceptionThrown
        throw new RuntimeException(exception);
	}

}
