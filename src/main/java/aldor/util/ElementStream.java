package aldor.util;

public interface ElementStream<T> {
	T peek();

	void next();

	boolean hasNext();
}
