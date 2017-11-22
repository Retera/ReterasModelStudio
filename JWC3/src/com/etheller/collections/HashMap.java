package com.etheller.collections;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public final class HashMap<KEY, VALUE> implements Map<KEY, VALUE> {
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	public static final int DEFAULT_DESIRED_CAPACITY = 1 << 4;

	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private Node<KEY, VALUE>[] table;

	private int threshold;
	private int capacityMinusOne;
	private int size;

	private int modCount;

	private CollectionView<VALUE> cachedValueCollection;
	private SetView<KEY> cachedKeySet;
	private SetView<Entry<KEY, VALUE>> cachedEntrySet;

	public HashMap() {
		this(DEFAULT_DESIRED_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public HashMap(final int desiredCapacity, final float loadFactor) {
		final int initialCapacity = tableSizeFor(desiredCapacity);
		table = createTable(initialCapacity);
		capacityMinusOne = table.length - 1;
		threshold = (int) (initialCapacity * loadFactor);
	}

	private Node<KEY, VALUE>[] createTable(final int size) {
		@SuppressWarnings("unchecked")
		final Node<KEY, VALUE>[] nodes = new Node[size];
		return nodes;
	}

	@Override
	public void clear() {
		Arrays.fill(table, null);
		size = 0;
	}

	private Node<KEY, VALUE> getNode(final KEY key) {
		final int hash = hash(key);
		final int index = indexFor(hash);
		Node<KEY, VALUE> node = table[index];
		while (node != null) {
			if (node.matches(hash, key)) {
				return node;
			}
			node = node.getNext();
		}
		return null;
	}

	private int indexFor(final int hash) {
		return hash & capacityMinusOne;
	}

	private static final int hash(final Object key) {
		return key == null ? 0 : key.hashCode();
	}

	@Override
	public boolean containsKey(final KEY key) {
		return getNode(key) != null;
	}

	@Override
	public boolean containsValue(final VALUE value) {
		for (int i = 0; i < table.length; i++) {
			Node<KEY, VALUE> node = table[i];
			while (node != null) {
				final VALUE nodeValue = node.getValue();
				if (matchingValues(value, nodeValue)) {
					return true;
				}
				node = node.getNext();
			}
		}
		return false;
	}

	private static boolean matchingValues(final Object value, final Object nodeValue) {
		return nodeValue == value || (value != null && value.equals(nodeValue));
	}

	@Override
	public SetView<MapView.Entry<KEY, VALUE>> entrySet() {
		if (cachedEntrySet == null) {
			cachedEntrySet = new EntrySetViewAdapter();
		}
		return cachedEntrySet;
	}

	@Override
	public VALUE get(final KEY key) {
		final Node<KEY, VALUE> node = getNode(key);
		return node == null ? null : node.getValue();
	}

	@Override
	public SetView<KEY> keySet() {
		if (cachedKeySet == null) {
			cachedKeySet = new KeySetViewAdapter();
		}
		return cachedKeySet;
	}

	@Override
	public VALUE put(final KEY key, final VALUE value) {
		final int hash = hash(key);
		final int index = indexFor(hash);
		Node<KEY, VALUE> node = table[index];
		while (node != null) {
			if (node.matches(hash, key)) {
				return node.replaceValue(value);
			}
			node = node.getNext();
		}
		node = new Node<>(hash, key, value, table[index]);
		table[index] = node;
		size++;
		modCount++;
		if (size > threshold) {
			resize();
		}
		return null;
	}

	private void resize() {
		final int oldCapacity = table.length;
		final Node<KEY, VALUE>[] newTable = createTable(oldCapacity << 1);
		capacityMinusOne = newTable.length - 1;
		threshold <<= 1;
		for (int i = 0; i < oldCapacity; i++) {
			Node<KEY, VALUE> node = table[i];
			if (node != null) {
				Node<KEY, VALUE> leftHead = null, rightHead = null;
				Node<KEY, VALUE> next;
				do {
					next = node.getNext();
					if ((node.getHash() & oldCapacity) == 0) {
						node.setNext(leftHead);
						leftHead = node;
					} else {
						node.setNext(rightHead);
						rightHead = node;
					}
				} while ((node = next) != null);
				if (leftHead != null) {
					newTable[i] = leftHead;
				}
				if (rightHead != null) {
					newTable[i | oldCapacity] = rightHead;
				}
			}
		}
		table = newTable;
	}

	@Override
	public VALUE remove(final KEY key) {
		final int hash = hash(key);
		final int index = indexFor(hash);
		Node<KEY, VALUE> node = table[index];
		Node<KEY, VALUE> prev = null;
		while (node != null) {
			if (node.matches(hash, key)) {
				if (prev == null) {
					table[index] = node.getNext();
				} else {
					prev.setNext(node.getNext());
				}
				size--;
				modCount++;
				return node.getValue();
			}
			prev = node;
			node = node.getNext();
		}
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public CollectionView<VALUE> values() {
		if (cachedValueCollection == null) {
			cachedValueCollection = new CollectionView<VALUE>() {
				@Override
				public Iterator<VALUE> iterator() {
					return new ValueIterator();
				}

				@Override
				public int size() {
					return HashMap.this.size();
				}

				@Override
				public boolean contains(final VALUE what) {
					return containsValue(what);
				}

				@Override
				public void forEach(final CollectionView.ForEach<? super VALUE> forEach) {
					final int expectedModCount = modCount;
					for (int i = 0; i < table.length; i++) {
						Node<KEY, VALUE> node = table[i];
						while (node != null) {
							if (modCount != expectedModCount) {
								throw new ConcurrentModificationException();
							}
							if (!forEach.onEntry(node.getValue())) {
								return;
							}

							node = node.getNext();
						}
					}
					if (modCount != expectedModCount) {
						throw new ConcurrentModificationException();
					}
				}

			};
		}
		return cachedValueCollection;
	}

	@Override
	public void forEach(final MapView.ForEach<? super KEY, ? super VALUE> forEach) {
		final int expectedModCount = modCount;
		for (int i = 0; i < table.length; i++) {
			Node<KEY, VALUE> node = table[i];
			while (node != null) {
				if (modCount != expectedModCount) {
					throw new ConcurrentModificationException();
				}
				if (!forEach.onEntry(node.getKey(), node.getValue())) {
					return;
				}

				node = node.getNext();
			}
		}
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}

	private final class EntrySetViewAdapter implements SetView<MapView.Entry<KEY, VALUE>> {
		@Override
		public int size() {
			return HashMap.this.size();
		}

		@Override
		public boolean contains(final MapView.Entry<KEY, VALUE> what) {
			final Node<KEY, VALUE> node = getNode(what.getKey());
			return node != null && matchingValues(node.getValue(), what.getValue());
		}

		@Override
		public void forEach(final ForEach<? super MapView.Entry<KEY, VALUE>> forEach) {
			final int expectedModCount = modCount;
			for (int i = 0; i < table.length; i++) {
				Node<KEY, VALUE> node = table[i];
				while (node != null) {
					if (modCount != expectedModCount) {
						throw new ConcurrentModificationException();
					}
					if (!forEach.onEntry(node)) {
						return;
					}

					node = node.getNext();
				}
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public Iterator<MapView.Entry<KEY, VALUE>> iterator() {
			return new EntryIterator();
		}
	}

	private final class KeySetViewAdapter implements SetView<KEY> {
		@Override
		public int size() {
			return HashMap.this.size();
		}

		@Override
		public boolean contains(final KEY what) {
			return containsKey(what);
		}

		@Override
		public void forEach(final ForEach<? super KEY> forEach) {
			final int expectedModCount = modCount;
			for (int i = 0; i < table.length; i++) {
				Node<KEY, VALUE> node = table[i];
				while (node != null) {
					if (modCount != expectedModCount) {
						throw new ConcurrentModificationException();
					}
					if (!forEach.onEntry(node.getKey())) {
						return;
					}

					node = node.getNext();
				}
			}
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public Iterator<KEY> iterator() {
			return new KeyIterator();
		}
	}

	@Override
	public Iterator<MapView.Entry<KEY, VALUE>> iterator() {
		return new EntryIterator();
	}

	private abstract class HashIterator<TYPE> implements Iterator<TYPE> {
		private int nextIndex;
		private Node<KEY, VALUE> node;

		public HashIterator() {
			advanceIndexAndNode();
		}

		protected final Entry<KEY, VALUE> nextEntry() {
			if (node == null) {
				throw new IllegalStateException("no more nodes for iterator");
			}
			final Node<KEY, VALUE> current = node;
			node = node.getNext();
			if (node == null) {
				advanceIndexAndNode();
			}
			return current;
		}

		@Override
		public boolean hasNext() {
			return node != null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("iterator is read only");
		}

		private void advanceIndexAndNode() {
			while (nextIndex < table.length && (node = table[nextIndex++]) == null) {

			}
		}
	}

	private final class KeyIterator extends HashIterator<KEY> {
		@Override
		public KEY next() {
			return nextEntry().getKey();
		}
	}

	private final class ValueIterator extends HashIterator<VALUE> {
		@Override
		public VALUE next() {
			return nextEntry().getValue();
		}
	}

	private final class EntryIterator extends HashIterator<Entry<KEY, VALUE>> {
		@Override
		public Entry<KEY, VALUE> next() {
			return nextEntry();
		}
	}

	/**
	 * Returns a power of two size for the given target capacity.
	 */
	private static final int tableSizeFor(final int capacity) {
		int numElements = capacity - 1;
		numElements |= numElements >>> 1;
		numElements |= numElements >>> 2;
		numElements |= numElements >>> 4;
		numElements |= numElements >>> 8;
		numElements |= numElements >>> 16;
		return (numElements < 0) ? 1 : (numElements >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : numElements + 1;
	}

	private static final class Node<KEY, VALUE> implements MapView.Entry<KEY, VALUE> {
		private final int hash;
		private final KEY key;
		private VALUE value;
		private Node<KEY, VALUE> next;

		public Node(final int hash, final KEY key, final VALUE value, final Node<KEY, VALUE> next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}

		public boolean matches(final int hash, final KEY key) {
			return hash == this.hash && (this.key == key || (this.key != null && this.key.equals(key)));
		}

		public int getHash() {
			return hash;
		}

		@Override
		public KEY getKey() {
			return key;
		}

		@Override
		public VALUE getValue() {
			return value;
		}

		public VALUE replaceValue(final VALUE value) {
			final VALUE previousValue = this.value;
			this.value = value;
			return previousValue;
		}

		public Node<KEY, VALUE> getNext() {
			return next;
		}

		public void setNext(final Node<KEY, VALUE> next) {
			this.next = next;
		}
	}
}
