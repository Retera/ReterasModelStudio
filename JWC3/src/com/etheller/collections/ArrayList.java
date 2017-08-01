package com.etheller.collections;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ArrayList<TYPE> implements List<TYPE> {
	private int modCount;
	private int size = 0;
	private TYPE[] elementData;

	public ArrayList() {
		this(10);
	}

	@SuppressWarnings("unchecked")
	public ArrayList(final int defaultSize) {
		if (defaultSize < 0) {
			throw new IllegalArgumentException("Negative size not allowed on ArrayList: " + defaultSize);
		}
		elementData = (TYPE[]) new Object[defaultSize];
	}

	public ArrayList(final CollectionView<TYPE> other) {
		this(10);
		for (final TYPE item : other) {
			this.add(item);
		}
	}

	@Override
	public TYPE get(final int index) {
		rangeCheck(index);
		return elementData[index];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(final TYPE item) {
		// TODO should be a Util, why are we implementing it
		return ListView.Util.contains(this, item);
	}

	@Override
	public void forEach(final CollectionView.ForEach<? super TYPE> forEach) {
		final int expectedModCount = modCount;
		for (int i = 0; i < size; i++) {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}

			if (!forEach.onEntry(elementData[i])) {
				break;
			}
		}
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}

	@Override
	public ModifyingIterator<TYPE> iterator() {
		return new ArrayListIterator();
	}

	@Override
	public boolean add(final TYPE item) {
		// should probably be util instead!
		ensureCapacity(size + 1);
		elementData[size++] = item;
		return true;
	}

	@Override
	public TYPE remove(final int index) {
		// should probably be util instead??
		rangeCheck(index);
		final TYPE prevValue = elementData[index];
		fastRemove(index);
		return prevValue;
	}

	@Override
	public boolean remove(final TYPE item) {
		// should probably be util instead!
		final int index = ListView.Util.indexOf(this, item);
		if (index == -1) {
			return false;
		}
		fastRemove(index);
		return true;
	}

	@Override
	public void clear() {
		modCount++;
		Arrays.fill(elementData, null);
		size = 0;
	}

	@Override
	public TYPE set(final int index, final TYPE value) {
		rangeCheck(index);

		final TYPE oldValue = elementData[index];
		elementData[index] = value;
		return oldValue;
	}

	private final void ensureCapacity(final int minCapacity) {
		modCount++;
		if (minCapacity >= elementData.length) {
			int newCapacity = (elementData.length * 3) / 2 + 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
	}

	private void fastRemove(final int index) {
		// inspiration from java.util source code
		modCount++;
		final int movedElementCount = size - index - 1;
		if (movedElementCount > 0) {
			System.arraycopy(elementData, index + 1, elementData, index, movedElementCount);
		}
		elementData[--size] = null;

	}

	private void rangeCheck(final int index) {
		if (index >= size || index < 0) {
			throw new IndexOutOfBoundsException(index + " not in [0," + (size - 1) + "]");
		}
	}

	private void rangeCheckForAdd(final int index) {
		if (index > size || index < 0) {
			throw new IndexOutOfBoundsException(index + " not in [0," + (size - 1) + "]");
		}
	}

	private final class ArrayListIterator implements ModifyingIterator<TYPE> {
		private final int expectedModCount = modCount;
		private int index = 0;
		private int lastIndex = -1;

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public TYPE next() {
			if (index >= size) {
				throw new NoSuchElementException();
			}
			checkConcurrentModification();
			return elementData[lastIndex = (index++)];
		}

		@Override
		public void remove() {
			throw new IllegalStateException("iterator is read only");
		}

		@Override
		public void delete() {
			// forward compatibility
			if (lastIndex == -1) {
				throw new IllegalStateException();
			}
			fastRemove(lastIndex);
			index = lastIndex;
			lastIndex = -1;
			checkConcurrentModification();
		}

		private void checkConcurrentModification() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public void add(final int index, final TYPE value) {
		rangeCheckForAdd(index);

		ensureCapacity(size + 1);
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = value;
		size++;
	}

	public ListIterator<TYPE> listIterator(final int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		return new ListItr(index);
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * <p>
	 * The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 *
	 * @see #listIterator(int)
	 */
	@Override
	public ListIterator<TYPE> listIterator() {
		return new ListItr(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a) {
		if (a.length < size) {
			// Make a new array of a's runtime type, but my contents:
			return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		}
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
	private class Itr implements Iterator<TYPE> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int expectedModCount = modCount;

		@Override
		public boolean hasNext() {
			return cursor != size;
		}

		@Override
		@SuppressWarnings("unchecked")
		public TYPE next() {
			checkForComodification();
			final int i = cursor;
			if (i >= size) {
				throw new NoSuchElementException();
			}
			final Object[] elementData = ArrayList.this.elementData;
			if (i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i + 1;
			return (TYPE) elementData[lastRet = i];
		}

		@Override
		public void remove() {
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			checkForComodification();

			try {
				ArrayList.this.remove(lastRet);
				cursor = lastRet;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	/**
	 * An optimized version of AbstractList.ListItr
	 */
	private class ListItr extends Itr implements ListIterator<TYPE> {
		ListItr(final int index) {
			super();
			cursor = index;
		}

		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		@SuppressWarnings("unchecked")
		public TYPE previous() {
			checkForComodification();
			final int i = cursor - 1;
			if (i < 0) {
				throw new NoSuchElementException();
			}
			final Object[] elementData = ArrayList.this.elementData;
			if (i >= elementData.length) {
				throw new ConcurrentModificationException();
			}
			cursor = i;
			return (TYPE) elementData[lastRet = i];
		}

		@Override
		public void set(final TYPE e) {
			if (lastRet < 0) {
				throw new IllegalStateException();
			}
			checkForComodification();

			try {
				ArrayList.this.set(lastRet, e);
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(final TYPE e) {
			checkForComodification();

			try {
				final int i = cursor;
				ArrayList.this.add(i, e);
				cursor = i + 1;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}
}
