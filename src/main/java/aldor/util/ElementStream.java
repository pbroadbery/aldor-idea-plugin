package aldor.util;

@SuppressWarnings("InterfaceNamingConvention")
public interface ElementStream<T> {
	T peek();

	void next();

	boolean hasNext();
}
