package com.etheller.warsmash.parsers.mdlx;

public enum InterpolationType {
	DONT_INTERP("DontInterp"), LINEAR("Linear"), HERMITE("Hermite"), BEZIER("Bezier");

	public static final InterpolationType[] VALUES = values();

	private String token;

    private InterpolationType(String token) {
		this.token = token;
	}

	public static InterpolationType getType(int whichValue) {
		return VALUES[whichValue];
	}

	public boolean tangential() {
		return ordinal() > 1;
	}

	public String toString() {
		return token;
	}
}
