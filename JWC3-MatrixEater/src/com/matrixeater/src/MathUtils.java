package com.matrixeater.src;

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
		final double min = Math.min(a,c);
		final double max = Math.max(a, c);
		return min < b && b < max;
	}
}
