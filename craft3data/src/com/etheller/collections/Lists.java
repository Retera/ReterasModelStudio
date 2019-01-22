package com.etheller.collections;

public final class Lists {
	public static <TYPE> List<TYPE> newList(final TYPE... items) {
		final List<TYPE> list = new ArrayList<>();
		for (final TYPE item : items) {
			list.add(item);
		}
		return list;
	}

	private static final ListView<?> emptyList = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public static <TYPE> ListView<TYPE> emptyList() {
		return (ListView<TYPE>) emptyList;
	}

	private Lists() {
	}
}
