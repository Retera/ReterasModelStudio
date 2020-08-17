package com.etheller.warsmash.parsers.mdlx;

public enum InterpolationType {
	DONT_INTERP(0), LINEAR(1), BEZIER(2), HERMITE(3);

	public static final InterpolationType[] VALUES = values();

	private int value;

    private InterpolationType(int whichValue) {
        value = whichValue;
	}
	
	public int getValue() {
		return value;
	}

	public static InterpolationType getType(int whichValue) {
		return VALUES[whichValue];
	}

	public boolean tangential() {
		return ordinal() > 1;
	}
}
