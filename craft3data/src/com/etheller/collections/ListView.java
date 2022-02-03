package com.etheller.collections;

import java.util.Arrays;

public interface ListView<TYPE> extends CollectionView<TYPE> {
	TYPE get(int index);

	<T> T[] toArray(T[] a);

	public final class Util {
		@SafeVarargs
		public static <TYPE> ListView<TYPE> of(final TYPE... array) {
			final List<TYPE> list = new ArrayList<>();
			for (final TYPE type : array) {
				list.add(type);
			}
			return list;
		}

		public static <TYPE> int indexOf(final ListView<TYPE> list, final TYPE item) {
			for (int i = 0; i < list.size(); i++) {
				final TYPE listItem = list.get(i);
				if (listItem == item || (listItem != null && listItem.equals(item))) {
					return i;
				}
			}
			return -1;
		}

		public static <TYPE> boolean contains(final ListView<TYPE> list, final TYPE item) {
			return indexOf(list, item) != -1;
		}
		
		public static <TYPE> boolean equalContents(ListView<TYPE> listOne, ListView<TYPE> listTwo) {
			if(listOne.size() != listTwo.size()) {
				return false;
			}
			for(int i = 0; i < listOne.size(); i++) {
				TYPE itemOne = listOne.get(i);
				TYPE itemTwo = listTwo.get(i);
				if(itemOne == null) {
					if(itemTwo != null) {
						return false;
					}
				} else if(!itemOne.equals(itemTwo)) {
					return false;
				}
			}
			return true;
		}

		public static <TYPE> TYPE[] toArray(final ListView<TYPE> list, TYPE[] array) {
			if (array.length < list.size()) {
				array = Arrays.copyOf(array, list.size());
			}
			int index = 0;
			for (final TYPE item : list) {
				array[index++] = item;
			}
			return array;
		}

		private Util() {
		}
	}
}
