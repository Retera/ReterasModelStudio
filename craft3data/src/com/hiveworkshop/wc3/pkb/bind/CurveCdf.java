package com.hiveworkshop.wc3.pkb.bind;

import java.util.ArrayList;
import java.util.List;

/** Builds the cumulative distribution table of a probability curve, as the engine does. */
public final class CurveCdf {
	private static final int MIN_SEGMENT_POINTS = 8;
	private static final int MAX_SEGMENT_POINTS = 256;
	private static final float DECIMATE_THRESHOLD = 0.0024999999f;

	private CurveCdf() {
	}

	private static float linearSpanIntegral(final float p0, final float p1, final float x0, final float x1) {
		return ((x1 - x0) * p0) + ((0.5f * (x1 - x0)) * (x1 + x0) * (p1 - p0));
	}

	private static float hermiteSpanIntegral(final float p0, final float p1, final float m0, final float m1,
			final float x0, final float x1) {
		final float kA = 0.5f;
		final float kB = 0.33333334f;
		final float t2 = x0 * x0;
		final float u2 = x1 * x1;
		final float t3 = (x0 * x0) * x0;
		final float u3 = (x1 * x1) * x1;
		final float t4 = (x0 * x0) * (x0 * x0);
		final float u4 = (x1 * x1) * (x1 * x1);
		final float s = x1 - x0;
		final float q2 = u2 - t2;
		final float q3 = u3 - t3;
		final float q4 = u4 - t4;
		final float a = p0 - p1;
		final float b = m0 + m1;
		final float head = (s * p0) + ((kA * q2) * m0);
		final float cubic = q3 * ((kB * (b + m0)) + a);
		final float quartic = (kA * q4) * ((kA * b) + a);
		return (head - cubic) + quartic;
	}

	private static float integrateCurveAt(final float[] times, final float[] values, final float[] tangents,
			final boolean hermite, final float x0Raw, final float x1Raw) {
		final float minT = times[0];
		final float maxT = times[times.length - 1];
		final float x0 = Math.min(x0Raw, x1Raw);
		final float x1 = Math.max(x0Raw, x1Raw);
		float acc = 0;
		if (x0 < minT) {
			if (x1 < minT) {
				return values[0] * (x1Raw - x0Raw);
			}
			acc = values[0] * (minT - x0);
		}
		for (int j = 1; j < times.length; j++) {
			if (x0 > times[j]) {
				continue;
			}
			final float c0 = times[j - 1];
			final float c1 = times[j];
			final float d = c1 - c0;
			if (d != 0) {
				final float rd = 1.0f / d;
				final float f0 = (Math.max(x0, c0) - c0) * rd;
				final float f1 = (Math.min(x1, c1) - c0) * rd;
				final float span = hermite
						? hermiteSpanIntegral(values[j - 1], values[j], tangents[(2 * j) - 1], tangents[2 * j], f0, f1)
						: linearSpanIntegral(values[j - 1], values[j], f0, f1);
				acc += span * d;
			}
			if (x1 <= times[j]) {
				break;
			}
		}
		if (x1 > maxT) {
			acc += values[values.length - 1] * (x1 - maxT);
		}
		return (x0Raw <= x1Raw) ? acc : -acc;
	}

	private static void decimateRecursive(final float[] times, final float[] values, final float threshold,
			final int indexStart, final int indexStop, final List<Integer> toRemove) {
		if ((indexStart + 2) >= indexStop) {
			return;
		}
		final float deltaT = times[indexStop - 1] - times[indexStart];
		if (Math.abs(deltaT) < 0.0000001f) {
			for (int i = indexStart + 1; i < (indexStop - 1); i++) {
				toRemove.add(i);
			}
			return;
		}
		final float invDeltaT = 1.0f / deltaT;
		final float slope = (values[indexStop - 1] - values[indexStart]) * invDeltaT;
		int indexMax = indexStart;
		float distanceMax = 0;
		for (int pos = indexStart + 1; pos < (indexStop - 1); pos++) {
			final float yvalue = values[indexStart] + (slope * (times[pos] - times[indexStart]));
			final float curDistance = Math.abs(values[pos] - yvalue);
			if (curDistance > distanceMax) {
				indexMax = pos;
				distanceMax = curDistance;
			}
		}
		if (distanceMax <= threshold) {
			for (int i = indexStart + 1; i < (indexStop - 1); i++) {
				toRemove.add(i);
			}
			return;
		}
		decimateRecursive(times, values, threshold, indexStart, indexMax, toRemove);
		decimateRecursive(times, values, threshold, indexMax, indexStop, toRemove);
	}

	private static int decimateCdf(final float[] times, final float[] values, final int count, final float threshold) {
		if (count < 2) {
			return count;
		}
		float lo = values[0];
		float hi = values[0];
		for (int i = 1; i < count; i++) {
			lo = Math.min(lo, values[i]);
			hi = Math.max(hi, values[i]);
		}
		final float range = hi - lo;
		final float curveScale = (range <= 0.000099999997f) ? 10000.0f : (1.0f / range);
		final List<Integer> toRemove = new ArrayList<>();
		decimateRecursive(times, values, curveScale * threshold, 0, count, toRemove);
		if (toRemove.isEmpty()) {
			return count;
		}
		int iRemove = 0;
		int iPoint = 0;
		for (int pos = 0; pos < count; pos++) {
			if ((iRemove < toRemove.size()) && (toRemove.get(iRemove) == pos)) {
				iRemove++;
				continue;
			}
			times[iPoint] = times[pos];
			values[iPoint] = values[pos];
			iPoint++;
		}
		return iPoint;
	}

	public static void build(final SamplerResource.Curve curve) {
		curve.cdfTimes = new float[0];
		curve.cdfValues = new float[0];
		if (!curve.isProbabilityCurve || (curve.components != 1)) {
			return;
		}
		final float[] srcTimes = curve.times;
		final float[] srcValues = curve.values;
		if ((srcTimes.length < 2) || (srcValues.length != srcTimes.length)) {
			return;
		}
		final float[] srcTangents = curve.tangents;
		if ((srcTangents.length != 0) && (srcTangents.length != (2 * srcTimes.length))) {
			return;
		}
		final boolean hermite = srcTangents.length != 0;
		final float timeStart = srcTimes[0];
		final float timeEnd = srcTimes[srcTimes.length - 1];
		final float timeRange = timeEnd - timeStart;
		final int[] segmentPoints = new int[srcTimes.length - 1];
		int total = 0;
		for (int i = 1; i < srcTimes.length; i++) {
			final boolean isLastKey = (i + 1) == srcTimes.length;
			final float timeDiff = srcTimes[i] - srcTimes[i - 1];
			final float segmentPointCount = ((256.0f * timeDiff) - timeStart) / timeRange;
			final int truncated = (int) segmentPointCount;
			final int nbPoints = Math.max(MIN_SEGMENT_POINTS,
					Math.min(MAX_SEGMENT_POINTS, (isLastKey ? 1 : 0) + truncated));
			segmentPoints[i - 1] = nbPoints;
			total += nbPoints;
		}
		if (total < 2) {
			return;
		}
		final float[] cdfTimes = new float[total];
		final float[] cdfValues = new float[total];
		int previousPointsSum = 0;
		for (int i = 1; i < srcTimes.length; i++) {
			final boolean isLastKey = (i + 1) == srcTimes.length;
			final float timeDiff = srcTimes[i] - srcTimes[i - 1];
			final int nbPoints = segmentPoints[i - 1];
			final int divisor = nbPoints - (isLastKey ? 1 : 0);
			final float invPointCount = timeDiff / divisor;
			for (int j = 0; j < nbPoints; j++) {
				cdfValues[previousPointsSum + j] = (j * invPointCount) + srcTimes[i - 1];
			}
			for (int j = 0; j < nbPoints; j++) {
				cdfTimes[previousPointsSum + j] = integrateCurveAt(srcTimes, srcValues, srcTangents, hermite, 0,
						cdfValues[previousPointsSum + j]);
			}
			previousPointsSum += nbPoints;
		}
		float realT0 = cdfTimes[0];
		for (int pos = 1; pos < total; pos++) {
			final float t0 = cdfTimes[pos - 1];
			final float t1 = cdfTimes[pos];
			final float newT = t0 + Math.max(0, t1 - realT0);
			realT0 = t1;
			cdfTimes[pos] = newT;
		}
		int count = total;
		if (cdfTimes[total - 1] <= 0) {
			cdfTimes[0] = 0;
			cdfTimes[1] = 1;
			cdfValues[0] = 0;
			cdfValues[1] = 1;
			count = 2;
		} else {
			final float rcpMaxTime = 1.0f / cdfTimes[total - 1];
			for (int i = 0; i < total; i++) {
				cdfTimes[i] = cdfTimes[i] * rcpMaxTime;
			}
			cdfTimes[total - 1] = 1;
			count = decimateCdf(cdfTimes, cdfValues, total, DECIMATE_THRESHOLD);
		}
		final float[] outT = new float[count];
		final float[] outV = new float[count];
		System.arraycopy(cdfTimes, 0, outT, 0, count);
		System.arraycopy(cdfValues, 0, outV, 0, count);
		curve.cdfTimes = outT;
		curve.cdfValues = outV;
	}
}
