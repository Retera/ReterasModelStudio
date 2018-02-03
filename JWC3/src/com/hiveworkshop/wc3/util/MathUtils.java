package com.hiveworkshop.wc3.util;

public class MathUtils {
	/**
	 * Returns true if the value of <code>b</code> falls between the values <code>a</code> and <code>c</code>.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static boolean isBetween(final double a, final double b, final double c) {
		final double min = Math.min(a, c);
		final double max = Math.max(a, c);
		return min < b && b < max;
	}

	public static double lerp(final double a, final double b, final double t) {
		return a + t * (b - a);
	}

	public static double hermite(final double a, final double aOutTan, final double bInTan, final double b,
			final double t) {
		final double factorTimes2 = t * t;
		final double factor1 = factorTimes2 * (2 * t - 3) + 1;
		final double factor2 = factorTimes2 * (t - 2) + 1;
		final double factor3 = factorTimes2 * (t - 1);
		final double factor4 = factorTimes2 * (3 - 2 * t);
		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}
}
