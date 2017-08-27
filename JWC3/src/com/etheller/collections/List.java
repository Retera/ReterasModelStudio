package com.etheller.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ListIterator;

public interface List<TYPE> extends ListView<TYPE>, Collection<TYPE> {
	TYPE set(int index, TYPE value);

	void add(int index, TYPE value);

	TYPE remove(int index);

	@Override
	ModifyingIterator<TYPE> iterator();

	ListIterator<TYPE> listIterator();

	public final class Util {

		public static <TYPE> void addAll(final List<TYPE> list, final ListView<TYPE> toAdd) {
			for (final TYPE item : toAdd) {
				list.add(item);
			}
		}

		public static <TYPE> void removeAll(final List<TYPE> list, final ListView<TYPE> toRemove) {
			for (final TYPE item : toRemove) {
				list.remove(item);
			}
		}

		public static <T> void sort(final List<T> list, final Comparator<? super T> c) {
			final Object[] a = list.toArray(new Object[0]);
			Arrays.sort(a, (Comparator) c);
			final ListIterator i = list.listIterator();
			for (int j = 0; j < a.length; j++) {
				i.next();
				i.set(a[j]);
			}
		}

		public static <T extends Comparable<T>> void sort(final List<T> list) {
			final Comparable[] a = list.toArray(new Comparable[0]);
			Arrays.sort(a);
			final ListIterator i = list.listIterator();
			for (int j = 0; j < a.length; j++) {
				i.next();
				i.set(a[j]);
			}
		}

		private Util() {
		}
	}
}
