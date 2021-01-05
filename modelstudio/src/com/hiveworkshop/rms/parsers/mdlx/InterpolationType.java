package com.hiveworkshop.rms.parsers.mdlx;

import java.util.Arrays;

public enum InterpolationType {
	DONT_INTERP("DontInterp"), LINEAR("Linear"), HERMITE("Hermite"), BEZIER("Bezier");

	public static final InterpolationType[] VALUES = values();

	private final String token;

	InterpolationType(final String token) {
		this.token = token;
	}

	public static InterpolationType getType(final int whichValue) {
		return VALUES[whichValue];
	}

	public boolean tangential() {
		return ordinal() > 1;
	}

	public static InterpolationType getType(String token) {
		return Arrays.stream(VALUES).filter(t -> t.token.equals(token)).findFirst().orElse(DONT_INTERP);
	}

	@Override
	public String toString() {
		return token;
	}
}
