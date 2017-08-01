package com.etheller.collections;

public interface CollectionView<T> extends Iterable<T> {
	int size();

	boolean contains(T what);

	void forEach(ForEach<? super T> forEach);

	public static interface ForEach<T> {
		boolean onEntry(T item);
	}

	public static final class Util {
		public static boolean isEmpty(final CollectionView<?> collection) {
			return collection.size() == 0;
		}

		public static <T> T[] toArray(final CollectionView<T> collection, final Class<T> clazz) {
			@SuppressWarnings("unchecked") // note that this is stupid and
											// shouldnt be how it is, but it
											// helps the JavaScript compiler GWT
											// system
			final T[] array = (T[]) new Object[collection.size()];// Array.newInstance(clazz,
																	// collection.size());
			collection.forEach(new ForEach<T>() {
				int arrayIndex = 0;

				@Override
				public boolean onEntry(final T item) {
					array[arrayIndex++] = item;
					return true;
				}
			});
			return array;
		}

		private Util() {
		}
	}
}
