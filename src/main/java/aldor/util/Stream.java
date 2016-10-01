package aldor.util;

public interface Stream<T> {
	T peek();

	void next();

	boolean hasNext();
}