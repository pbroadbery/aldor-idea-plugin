package aldor.util;

@SuppressWarnings("InterfaceNamingConvention")
public interface Stream<T> {
	T peek();

	void next();

	boolean hasNext();
}