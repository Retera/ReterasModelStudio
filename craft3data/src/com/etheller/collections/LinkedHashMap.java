package com.etheller.collections;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Very bad implementation that uses a bunch of garbage wrapper objects to pretend to be a linked hash map
 * implementation by using a java linked hash map.
 *
 * @author Eric
 *
 * @param <KEY>
 * @param <VALUE>
 */
public class LinkedHashMap<KEY, VALUE> implements Map<KEY, VALUE> {
	private final class KeySetViewImplementation implements SetView<KEY> {
		private final java.util.Set<KEY> javaSet = javaMap.keySet();

		@Override
		public int size() {
			return javaSet.size();
		}

		@Override
		public boolean contains(final KEY what) {
			return javaSet.contains(what);
		}

		@Override
		public void forEach(final com.etheller.collections.CollectionView.ForEach<? super KEY> forEach) {
			for (final KEY key : javaSet) {
				if (!forEach.onEntry(key)) {
					break;
				}
			}
		}

		@Override
		public Iterator<KEY> iterator() {
			return javaSet.iterator();
		}
	}

	private final class EntrySetViewImplementation implements SetView<MapView.Entry<KEY, VALUE>> {
		private final java.util.Set<java.util.Map.Entry<KEY, VALUE>> javaEntrySet = javaMap.entrySet();

		@Override
		public int size() {
			return javaEntrySet.size();
		}

		@Override
		public boolean contains(final com.etheller.collections.MapView.Entry<KEY, VALUE> what) {
			return javaEntrySet.contains(what);
		}

		@Override
		public void forEach(
				final com.etheller.collections.CollectionView.ForEach<? super com.etheller.collections.MapView.Entry<KEY, VALUE>> forEach) {
			for (final java.util.Map.Entry<KEY, VALUE> javaEntry : javaEntrySet) {
				if (!forEach.onEntry(new AbstractMap.SimpleEntry<>(javaEntry.getKey(), javaEntry.getValue()))) {
					break;
				}
			}
		}

		@Override
		public Iterator<com.etheller.collections.MapView.Entry<KEY, VALUE>> iterator() {
			return new Iterator<MapView.Entry<KEY, VALUE>>() {
				Iterator<java.util.Map.Entry<KEY, VALUE>> javaIterator = javaEntrySet.iterator();

				@Override
				public boolean hasNext() {
					return javaIterator.hasNext();
				}

				@Override
				public com.etheller.collections.MapView.Entry<KEY, VALUE> next() {
					final java.util.Map.Entry<KEY, VALUE> javaNext = javaIterator.next();
					return new AbstractMap.SimpleEntry<>(javaNext.getKey(), javaNext.getValue());
				}
			};
		}
	}

	private final class CollectionViewImplementation implements CollectionView<VALUE> {
		private final java.util.Collection<VALUE> javaValues = javaMap.values();

		@Override
		public Iterator<VALUE> iterator() {
			return javaValues.iterator();
		}

		@Override
		public int size() {
			return javaValues.size();
		}

		@Override
		public boolean contains(final VALUE what) {
			return javaValues.contains(what);
		}

		@Override
		public void forEach(final com.etheller.collections.CollectionView.ForEach<? super VALUE> forEach) {
			for (final VALUE value : javaValues) {
				if (!forEach.onEntry(value)) {
					break;
				}
			}
		}
	}

	private final java.util.LinkedHashMap<KEY, VALUE> javaMap = new java.util.LinkedHashMap<>();
	private KeySetViewImplementation keySetViewImplementation;
	private EntrySetViewImplementation entrySetViewImplementation;
	private CollectionViewImplementation collectionViewImplementation;

	@Override
	public boolean equals(final Object o) {
		return javaMap.equals(o);
	}

	@Override
	public boolean containsValue(final Object value) {
		return javaMap.containsValue(value);
	}

	@Override
	public int hashCode() {
		return javaMap.hashCode();
	}

	@Override
	public VALUE get(final Object key) {
		return javaMap.get(key);
	}

	@Override
	public String toString() {
		return javaMap.toString();
	}

	public VALUE getOrDefault(final Object key, final VALUE defaultValue) {
		return javaMap.getOrDefault(key, defaultValue);
	}

	@Override
	public void clear() {
		javaMap.clear();
	}

	@Override
	public int size() {
		return javaMap.size();
	}

	public boolean isEmpty() {
		return javaMap.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return javaMap.containsKey(key);
	}

	@Override
	public VALUE put(final KEY key, final VALUE value) {
		return javaMap.put(key, value);
	}

	public void forEach(final BiConsumer<? super KEY, ? super VALUE> action) {
		javaMap.forEach(action);
	}

	public void replaceAll(final BiFunction<? super KEY, ? super VALUE, ? extends VALUE> function) {
		javaMap.replaceAll(function);
	}

	public void putAll(final java.util.Map<? extends KEY, ? extends VALUE> m) {
		javaMap.putAll(m);
	}

	@Override
	public VALUE remove(final Object key) {
		return javaMap.remove(key);
	}

	public VALUE putIfAbsent(final KEY key, final VALUE value) {
		return javaMap.putIfAbsent(key, value);
	}

	public boolean remove(final Object key, final Object value) {
		return javaMap.remove(key, value);
	}

	public boolean replace(final KEY key, final VALUE oldValue, final VALUE newValue) {
		return javaMap.replace(key, oldValue, newValue);
	}

	public VALUE replace(final KEY key, final VALUE value) {
		return javaMap.replace(key, value);
	}

	public VALUE computeIfAbsent(final KEY key, final Function<? super KEY, ? extends VALUE> mappingFunction) {
		return javaMap.computeIfAbsent(key, mappingFunction);
	}

	public VALUE computeIfPresent(final KEY key,
			final BiFunction<? super KEY, ? super VALUE, ? extends VALUE> remappingFunction) {
		return javaMap.computeIfPresent(key, remappingFunction);
	}

	public VALUE compute(final KEY key,
			final BiFunction<? super KEY, ? super VALUE, ? extends VALUE> remappingFunction) {
		return javaMap.compute(key, remappingFunction);
	}

	public VALUE merge(final KEY key, final VALUE value,
			final BiFunction<? super VALUE, ? super VALUE, ? extends VALUE> remappingFunction) {
		return javaMap.merge(key, value, remappingFunction);
	}

	@Override
	public Object clone() {
		return javaMap.clone();
	}

	@Override
	public SetView<KEY> keySet() {
		if (keySetViewImplementation == null) {
			keySetViewImplementation = new KeySetViewImplementation();
		}
		return keySetViewImplementation;
	}

	@Override
	public EntrySetViewImplementation entrySet() {
		if (entrySetViewImplementation == null) {
			entrySetViewImplementation = new EntrySetViewImplementation();
		}
		return entrySetViewImplementation;
	}

	@Override
	public CollectionView<VALUE> values() {
		if (collectionViewImplementation == null) {
			collectionViewImplementation = new CollectionViewImplementation();
		}
		return collectionViewImplementation;
	}

	@Override
	public void forEach(final com.etheller.collections.MapView.ForEach<? super KEY, ? super VALUE> forEach) {
		final EntrySetViewImplementation entrySet = entrySet();
		for (final java.util.Map.Entry<KEY, VALUE> entry : entrySet.javaEntrySet) {
			if (!forEach.onEntry(entry.getKey(), entry.getValue())) {
				break;
			}
		}
	}

	@Override
	public Iterator<com.etheller.collections.MapView.Entry<KEY, VALUE>> iterator() {
		return entrySet().iterator();
	}

}
