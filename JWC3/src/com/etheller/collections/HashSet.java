package com.etheller.collections;

import java.util.Iterator;

public class HashSet<TYPE> implements Set<TYPE> {
	private static final Object PRESENT = new Object();

	private final HashMap<TYPE, Object> map;

	public HashSet() {
		map = new HashMap<>();
	}

	public HashSet(final Collection<TYPE> stuff) {
		map = new HashMap<>(stuff.size(), HashMap.DEFAULT_LOAD_FACTOR);
		for (final TYPE element : stuff) {
			add(element);
		}
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean contains(final TYPE what) {
		return map.keySet().contains(what);
	}

	@Override
	public void forEach(final CollectionView.ForEach<? super TYPE> forEach) {
		map.keySet().forEach(forEach);
	}

	@Override
	public Iterator<TYPE> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean add(final TYPE what) {
		return map.put(what, PRESENT) == null;
	}

	@Override
	public boolean remove(final TYPE what) {
		return map.remove(what) != null;
	}

	@Override
	public void clear() {
		map.clear();
	}
}
