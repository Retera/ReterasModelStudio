package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.List;

/**
 * Identity-based list helpers. Several model classes override equals (Bitmap,
 * Material, Layer) and Integer global sequences compare by value, so the usual
 * List.remove(Object) can remove the wrong element.
 */
final class ComponentListUtil {
	private ComponentListUtil() {
	}

	static int indexOfIdentity(final List<?> list, final Object item) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == item) {
				return i;
			}
		}
		return -1;
	}

	/** @return the index the item was at, or -1 if it was not present */
	static int removeIdentity(final List<?> list, final Object item) {
		final int index = indexOfIdentity(list, item);
		if (index >= 0) {
			list.remove(index);
		}
		return index;
	}

	static <T> void insertAt(final List<T> list, final int index, final T item) {
		if ((index < 0) || (index > list.size())) {
			list.add(item);
		} else {
			list.add(index, item);
		}
	}
}
