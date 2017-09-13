package com.etheller.util;

import javax.swing.DefaultListModel;

import com.etheller.collections.ArrayList;
import com.etheller.collections.CollectionView;
import com.etheller.collections.HashSet;
import com.etheller.collections.List;
import com.etheller.collections.Set;

public final class CollectionUtils {

	public static <T> java.util.List<T> asList(final DefaultListModel<T> thing) {
		final java.util.List<T> list = new java.util.ArrayList<>();
		for (int i = 0; i < thing.size(); i++) {
			list.add(thing.get(i));
		}
		return list;
	}

	public static <T> List<T> asList(final java.util.Collection<T> collection) {
		final List<T> list = new ArrayList<>();
		for (final T element : collection) {
			list.add(element);
		}
		return list;
	}

	public static <T> java.util.Collection<T> toJava(final CollectionView<T> collection) {
		final java.util.List<T> list = new java.util.ArrayList<>();
		for (final T element : collection) {
			list.add(element);
		}
		return list;
	}

	private CollectionUtils() {
	}

	public static <T> Set<T> asSet(final java.util.Collection<T> collection) {
		final Set<T> resultSet = new HashSet<>();
		for (final T element : collection) {
			resultSet.add(element);
		}
		return resultSet;
	}
}
