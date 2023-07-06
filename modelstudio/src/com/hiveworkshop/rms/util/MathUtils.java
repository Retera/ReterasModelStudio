package com.hiveworkshop.rms.util;

public class MathUtils {
	/**
	 * Returns true if the value of <code>value</code> falls between, but not on, the values
	 * <code>endPoint1</code> and <code>endPoint2</code>.
	 * @return <code>endPoint1 < value < endPoint2</code>
	 */
	public static boolean isBetween(double endPoint1, double endPoint2, double value) {
		double min = Math.min(endPoint1, endPoint2);
		double max = Math.max(endPoint1, endPoint2);
		return (min < value) && (value < max);
	}

	/**
	 * Returns true if the value of <code>value</code> falls between the values
	 * <code>endPoint1</code> and <code>endPoint2</code>.
	 * @return <code>endPoint1 <= value <= endPoint2</code>
	 */
	public static boolean isBetweenInc(double endPoint1, double endPoint2, double value) {
		double min = Math.min(endPoint1, endPoint2);
		double max = Math.max(endPoint1, endPoint2);
		return (min <= value) && (value <= max);
	}

	public static int clamp(int value, int min, int max){
		return Math.min(Math.max(min, value), max);
	}
	public static float clamp(float value, float min, float max){
		return Math.min(Math.max(min, value), max);
	}
	public static double clamp(double value, double min, double max){
		return Math.min(Math.max(min, value), max);
	}

	public static float lerp(float floorValue, float ceilValue, float timeFraction) {
		return floorValue + (timeFraction * (ceilValue - floorValue));
	}

	public static float hermite(float floorValue, float floorOutTan, float ceilInTan, float ceilValue, float timeFraction) {
		float tfSq = timeFraction * timeFraction;

		float factor1 = tfSq * ((2 * timeFraction) - 3) + 1;
		float factor2 = tfSq * (timeFraction - 2) + timeFraction;
		float factor3 = tfSq * (timeFraction - 1);
		float factor4 = tfSq * (3 - (2 * timeFraction));

		return (floorValue * factor1) + (floorOutTan * factor2) + (ceilInTan * factor3) + (ceilValue * factor4);
	}

	public static float bezier(float floorValue, float floorOutTan, float ceilInTan, float ceilValue, float timeFraction) {
		float tfInv = 1 - timeFraction;
		float tfSq = timeFraction * timeFraction;
		float tfInvSq = tfInv * tfInv;

		float factor1 = tfInvSq * tfInv;
		float factor2 = 3 * timeFraction * tfInvSq;
		float factor3 = 3 * tfSq * tfInv;
		float factor4 = tfSq * timeFraction;

		return (floorValue * factor1) + (floorOutTan * factor2) + (ceilInTan * factor3) + (ceilValue * factor4);
	}

	public static float randomInRange(double min, double max) {
		return (float) (min + (Math.random() * (max - min)));
	}
	public static float randomSym(double maxDiv) {
		return randomInRange(-maxDiv, maxDiv);
//		return (float) ((Math.random() * (2*maxDiv) - maxDiv));
	}

	public static int uint8ToUint16(byte b16, byte b8) {
		return ((b16 << 8) & 0xFF00) | (b8 & 0xFF);
	}
	public static int uint8ToUint24(byte right, byte bottom, byte a) {
		return ((right << 16) & 0xFF0000) | ((bottom << 8) & 0xFF00) | (a & 0xFF);
	}
	public static int uint8ToUint32(byte b32, byte b24, byte b16, byte b8) {
		return ((b32 << 24) & 0xFF000000) | ((b24 << 16) & 0xFF0000) | ((b16 << 8) & 0xFF00) | (b8 & 0xFF);
	}
}
