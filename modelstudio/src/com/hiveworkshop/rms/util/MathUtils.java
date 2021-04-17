package com.hiveworkshop.rms.util;

public class MathUtils {
	/**
	 * Returns true if the value of <code>value</code> falls between the values
	 * <code>endPoint1</code> and <code>endPoint2</code>.
	 */
	public static boolean isBetween(final double endPoint1, final double endPoint2, final double value) {
		final double min = Math.min(endPoint1, endPoint2);
		final double max = Math.max(endPoint1, endPoint2);
		return (min < value) && (value < max);
	}

	public static float lerp(final float a, final float b, final float t) {
		return a + (t * (b - a));
	}

	public static float hermite(final float a, final float aOutTan, final float bInTan, final float b,
			final float t) {
		final float factorTimes2 = t * t;
		final float factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final float factor2 = (factorTimes2 * (t - 2)) + t;
		final float factor3 = factorTimes2 * (t - 1);
		final float factor4 = factorTimes2 * (3 - (2 * t));
		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	public static float bezier(final float a, final float aOutTan, final float bInTan, final float b,
			final float t) {
		final float invt = 1 - t;
		final float factorSquared = t * t;
		final float inverseFactorSquared = invt * invt;
		final float factor1 = inverseFactorSquared * invt;
		final float factor2 = 3 * t * inverseFactorSquared;
		final float factor3 = 3 * factorSquared * invt;
		final float factor4 = factorSquared * t;

		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	public static float randomInRange(final double min, final double max) {
		return (float) (min + (Math.random() * (max - min)));
	}

	public static int uint8ToUint24(final byte right, final byte bottom, final byte a) {
		return ((right << 16) & 0xFF0000) | ((bottom << 8) & 0xFF00) | (a & 0xFF);
	}
}
