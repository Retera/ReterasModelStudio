package com.hiveworkshop.rms.util;

import java.util.Objects;

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

	@Override
	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Object first = ((Pair<?, ?>) other).getFirst();
			Object second = ((Pair<?, ?>) other).getSecond();
			return getFirst() == first && getSecond() == second || getFirst() == second && getSecond() == first;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first) + Objects.hash(second);
	}
}
