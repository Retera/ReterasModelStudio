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
			Object oFirst = ((Pair<?, ?>) other).getFirst();
			Object oSecond = ((Pair<?, ?>) other).getSecond();
			if(first != null){
				if(second != null){
					return first.equals(oFirst) && second.equals(oSecond)
							|| first.equals(oSecond) && second.equals(oFirst);
				} else {
					return first.equals(oFirst) && oSecond == null
							|| first.equals(oSecond) && oFirst == null;
				}
			} else {
				if(second != null){
					return oFirst == null && second.equals(oSecond)
							|| oSecond == null && second.equals(oFirst);
				} else {
					return oFirst == null && oSecond == null;
				}
			}
//			Object first = ((Pair<?, ?>) other).getFirst();
//			Object second = ((Pair<?, ?>) other).getSecond();
//			return getFirst() == first && getSecond() == second || getFirst() == second && getSecond() == first;
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(first != null && second != null){
			return first.hashCode() + second.hashCode();
		}
		return Objects.hash(first) + Objects.hash(second);
	}
}
