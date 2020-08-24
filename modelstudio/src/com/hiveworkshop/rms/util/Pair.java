package com.hiveworkshop.rms.util;

public final class Pair<FIRST, SECOND> {
	private final FIRST first;
	private final SECOND second;

	public Pair(final FIRST first, final SECOND second) {
		this.first = first;
		this.second = second;
	}

	public FIRST getFirst() {
		return first;
	}

	public SECOND getSecond() {
		return second;
	}
}
