package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/*
 * An extended version of DefaultListModel.
 * Supports addAll, foreach and initialization with an other IterableListModel or Collection
 */

public class IterableListModel<T> extends DefaultListModel<T> implements Iterable<T> {

	public IterableListModel() {
		super();
	}

	public IterableListModel(IterableListModel<? extends T> c) {
		this();
		addAll(c);
	}

	public IterableListModel(Collection<? extends T> c) {
		this();
		addAll(c);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public T next() {
				return get(i++);
			}
		};
	}

	public void remove(T t) {
		for (int i = 0; i < size(); i++) {
			if (get(i) == t || get(i).equals(t)) {
				remove(i);
				return;
			}
		}
	}

	public void removeAll(IterableListModel<? extends T> c) {
		for (T t : c) {
			remove(t);
		}
	}

	public void removeAll(Collection<? extends T> c) {
		for (T t : c) {
			remove(t);
		}
	}


	public void addAll(IterableListModel<? extends T> c) {
		if (c.size() > 0) {
			c.forEach(this::addElement);
		}
	}

	public void addAll(Collection<? extends T> c) {
		c.forEach(this::addElement);
	}

	public List<T> asList() {
		List<T> newList = new ArrayList<>();
		for (T t : this) {
			newList.add(t);
		}
		return newList;
	}
}
