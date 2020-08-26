package com.hiveworkshop.rms.util;

public class MathUtils {
	/**
	 * Returns true if the value of <code>b</code> falls between the values
	 * <code>a</code> and <code>c</code>.
	 *
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static boolean isBetween(final double a, final double b, final double c) {
		final double min = Math.min(a, c);
		final double max = Math.max(a, c);
		return (min < b) && (b < max);
	}

	public static double lerp(final double a, final double b, final double t) {
		return a + (t * (b - a));
	}

	public static Vector3 lerp(final Vector3 out, final Vector3 a, final Vector3 b, final double t) {
		out.set(MathUtils.lerp(a.x, b.x, t), MathUtils.lerp(a.y, b.y, t), MathUtils.lerp(a.z, b.z, t));
		return out;
	}

	public static Vector4 lerp(final Vector4 out, final Vector4 a, final Vector4 b, final double t) {
		out.set((float) MathUtils.lerp(a.x, b.x, t), (float) MathUtils.lerp(a.y, b.y, t),
				(float) MathUtils.lerp(a.z, b.z, t), (float) MathUtils.lerp(a.w, b.w, t));
		return out;
	}

	public static double hermite(final double a, final double aOutTan, final double bInTan, final double b,
			final double t) {
		final double factorTimes2 = t * t;
		final double factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final double factor2 = (factorTimes2 * (t - 2)) + t;
		final double factor3 = factorTimes2 * (t - 1);
		final double factor4 = factorTimes2 * (3 - (2 * t));
		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	public static double bezier(final double a, final double aOutTan, final double bInTan, final double b,
			final double t) {
		final double invt = 1 - t;
		final double factorSquared = t * t;
		final double inverseFactorSquared = invt * invt;
		final double factor1 = inverseFactorSquared * invt;
		final double factor2 = 3 * t * inverseFactorSquared;
		final double factor3 = 3 * factorSquared * invt;
		final double factor4 = factorSquared * t;

		return (a * factor1) + (aOutTan * factor2) + (bInTan * factor3) + (b * factor4);
	}

	public static float randomInRange(final double min, final double max) {
		return (float) (min + (Math.random() * (max - min)));
	}

	public static int uint8ToUint24(final byte right, final byte bottom, final byte a) {
		return ((right << 16) & 0xFF0000) | ((bottom << 8) & 0xFF00) | (a & 0xFF);
	}
}
