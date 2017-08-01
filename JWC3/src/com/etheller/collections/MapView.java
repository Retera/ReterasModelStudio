package com.etheller.collections;

public interface MapView<KEY, VALUE> extends Iterable<MapView.Entry<KEY, VALUE>> {
	SetView<KEY> keySet();

	SetView<Entry<KEY, VALUE>> entrySet();

	VALUE get(KEY key);

	boolean containsKey(KEY key);

	boolean containsValue(VALUE value);

	CollectionView<VALUE> values();

	int size();

	void forEach(MapView.ForEach<? super KEY, ? super VALUE> forEach);

	public static interface ForEach<K, V> {
		boolean onEntry(K key, V value);
	}

	public static interface Entry<K, V> {
		K getKey();

		V getValue();
	}

	public static final class Util {
		public static boolean isEmpty(final MapView<?, ?> map) {
			return map.size() == 0;
		}

		private Util() {
		}
	}
}
