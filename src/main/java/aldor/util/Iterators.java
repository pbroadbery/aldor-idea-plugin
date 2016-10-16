package aldor.util;

import java.util.Iterator;
import java.util.ListIterator;

public final class Iterators {

	public static final <T> ListIterator<T> listIterator(final Iterator<T> iterator) {
		//noinspection OverlyComplexAnonymousInnerClass
		return new ListIterator<T>() {
			private int index = 0;
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				index++;
				return iterator.next();
			}

			@Override
			public boolean hasPrevious() {
				throw new UnsupportedOperationException("Can't go back");
			}

			@Override
			public T previous() {
				throw new UnsupportedOperationException("can't go back");
			}

			@Override
			public int nextIndex() {
				return index + 1;
			}

			@Override
			public int previousIndex() {
				throw new UnsupportedOperationException("can't go back");
			}

			@Override
			public void remove() {
				iterator.remove();
			}

			@Override
			public void set(T e) {
				throw new UnsupportedOperationException("Read only");
			}

			@Override
			public void add(T e) {
				throw new UnsupportedOperationException("Read only");
			}};

	}
}
