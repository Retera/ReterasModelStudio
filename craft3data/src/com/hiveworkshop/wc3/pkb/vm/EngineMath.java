package com.hiveworkshop.wc3.pkb.vm;

/** Bit-exact ports of the engine's fast float routines (exp2, sin/cos, rcp). */
public final class EngineMath {
	private EngineMath() {
	}

	public static int truncateToInt(final float v) {
		if (!(v >= -2147483648.0f) || !(v < 2147483648.0f)) {
			return 0x80000000;
		}
		return (int) v;
	}

	private static final float EXP2_CLAMP_MIN = Float.intBitsToFloat(0xC2FDFFFF);
	private static final float EXP2_CLAMP_MAX = Float.intBitsToFloat(0x4300FFFF);
	private static final float EXP2_C0 = Float.intBitsToFloat(0x3F800000);
	private static final float EXP2_C1 = Float.intBitsToFloat(0x3F31727B);
	private static final float EXP2_C2 = Float.intBitsToFloat(0x3E75EAD4);
	private static final float EXP2_C3 = Float.intBitsToFloat(0x3D64AA23);
	private static final float EXP2_C4 = Float.intBitsToFloat(0x3C134806);
	private static final float EXP2_C5 = Float.intBitsToFloat(0x3AF61905);
	private static final float INV_LN2 = Float.intBitsToFloat(0x3FB8AA3B);

	public static float exp2(final float x) {
		final float lo = (x < EXP2_CLAMP_MAX) ? x : EXP2_CLAMP_MAX;
		final float safeInput = (lo > EXP2_CLAMP_MIN) ? lo : EXP2_CLAMP_MIN;
		final float fl = (float) Math.floor(safeInput);
		final int rInt = truncateToInt(fl);
		final float t = safeInput - fl;
		final float e = Float.intBitsToFloat((rInt + 127) << 23);
		final float t2 = t * t;
		final float p0 = (t * EXP2_C5) + EXP2_C4;
		final float p1 = (t * EXP2_C3) + EXP2_C2;
		final float p2 = (t * EXP2_C1) + EXP2_C0;
		final float p3 = (t2 * p0) + p1;
		final float p4 = (t2 * p3) + p2;
		return p4 * e;
	}

	public static float exp(final float x) {
		return exp2(x * INV_LN2);
	}

	private static final float FOUR_OVER_PI = Float.intBitsToFloat(0x3FA2F983);
	private static final float DP1 = Float.intBitsToFloat(0xBF490000);
	private static final float DP2 = Float.intBitsToFloat(0xB97DA000);
	private static final float DP3 = Float.intBitsToFloat(0xB3222169);
	private static final float SIN_C0 = Float.intBitsToFloat(0xB94CA1F9);
	private static final float SIN_C1 = Float.intBitsToFloat(0x3C08839E);
	private static final float SIN_C2 = Float.intBitsToFloat(0xBE2AAAA3);
	private static final float NEG_HALF = Float.intBitsToFloat(0xBF000000);
	private static final float COS_C0 = Float.intBitsToFloat(0x37CCF5CE);
	private static final float COS_C1 = Float.intBitsToFloat(0xBAB6061A);
	private static final float COS_C2 = Float.intBitsToFloat(0x3D2AAAA5);
	private static final float SINCOS_MAX = Float.intBitsToFloat(0x4B000000);
	private static final int SIGN_MASK = 0x80000000;
	private static final int ABS_MASK = 0x7FFFFFFF;

	/** Writes sin(x) to out[0] and cos(x) to out[1]. */
	public static void sinCos(final float xo, final float[] out) {
		float x = Float.intBitsToFloat(Float.floatToRawIntBits(xo) & ABS_MASK);
		final int overflowMask = (x < SINCOS_MAX) ? 0xFFFFFFFF : 0;
		float y = x * FOUR_OVER_PI;
		int emm2 = truncateToInt(y);
		emm2 = (emm2 + 1) & ~1;
		y = emm2;
		x = (y * DP1) + x;
		x = (y * DP2) + x;
		x = (y * DP3) + x;
		x = Float.intBitsToFloat(Float.floatToRawIntBits(x) & overflowMask);
		final int enn2 = emm2 - 2;
		final boolean poly = (emm2 & 2) != 0;
		final int bt4 = emm2 << 29;
		final int bt5 = enn2 << 29;
		final int sinSignMask = SIGN_MASK & (Float.floatToRawIntBits(xo) ^ bt4);
		final int cosSignMask = SIGN_MASK & ~bt5;
		final float x2 = x * x;
		final float x3 = x2 * x;
		final float x4 = x2 * x2;
		final float cR = (x2 * NEG_HALF) + 1.0f;
		float y1 = COS_C0;
		float y2 = SIN_C0;
		y1 = (y1 * x2) + COS_C1;
		y2 = (y2 * x2) + SIN_C1;
		y1 = (y1 * x2) + COS_C2;
		y2 = (y2 * x2) + SIN_C2;
		y1 = (y1 * x4) + cR;
		y2 = (y2 * x3) + x;
		final float ys = poly ? y1 : y2;
		final float yc = poly ? y2 : y1;
		out[0] = Float.intBitsToFloat(Float.floatToRawIntBits(ys) ^ sinSignMask);
		out[1] = Float.intBitsToFloat(Float.floatToRawIntBits(yc) ^ cosSignMask);
	}

	public static float rcp(final float x) {
		final float r = 1.0f / x;
		final int zMask = (x != 0.0f) ? 0xFFFFFFFF : 0;
		final int rMask = (r != 0.0f) ? 0xFFFFFFFF : 0;
		final int term = Float.floatToRawIntBits((x * r) * r) & zMask & rMask;
		return (r + r) - Float.intBitsToFloat(term);
	}

	public static float sqrt(final float x) {
		return (float) Math.sqrt(Math.abs(x));
	}

	public static float rsqrt(final float x) {
		return 1.0f / (float) Math.sqrt(Math.abs(x));
	}
}
