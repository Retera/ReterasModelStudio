package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.*;

import java.util.TreeMap;

/**
 * Aiming to copy some behaviors from Mdlvis with this
 *
 * Comments are meant as translation from Mdlvis, but then I got lazy and
 * stopped asking Max for Russian to English translations on some of the
 * functions, because mostly I just needed to translate Pascal to Java.
 *
 * ===================================================
 *
 * Is used for calculating the Tangential part of KK
 */
public abstract class TTan<T> {
	AnimFlag<T> timeline;
	public float bias;
	public float tension;
	public float continuity; // Spline parameters
	public Entry<T> prev;
	public Entry<T> tang; // storage for tangents
	public Entry<T> cur;
	public Entry<T> next; // Values in KK
	protected T deltaOut;
	protected T deltaIn;
	protected T logNNP;
	protected T logNMN;

	public boolean isLogsReady; // "true" if Logarithms are ready

	public TTan(AnimFlag<T> timeline) {
		this.timeline = timeline;
		Entry<T> value = timeline.getEntryMap().firstEntry().getValue();
		tang = new Entry<>(value);
		cur = new Entry<>(value);
		prev = new Entry<>(value);
		next = new Entry<>(value);
	}

	public void setFromKF(int time) {
		setFromKF2(time, timeline);
	}

	public static <Q> TTan<Q> getNewTTan(AnimFlag<Q> timeline) {
		if (timeline instanceof FloatAnimFlag) {
			return (TTan<Q>) new TTanFloat((FloatAnimFlag) timeline);
		} else if (timeline instanceof Vec3AnimFlag) {
			return (TTan<Q>) new TTanVec3((Vec3AnimFlag) timeline);
		} else if (timeline instanceof QuatAnimFlag) {
			return (TTan<Q>) new TTanQuat((QuatAnimFlag) timeline);
		}
		return null;
	}

	public static TTan<?> getNewTTan2(AnimFlag<?> timeline) {
		if (timeline instanceof FloatAnimFlag) {
			return new TTanFloat((FloatAnimFlag) timeline);
		} else if (timeline instanceof Vec3AnimFlag) {
			return new TTanVec3((Vec3AnimFlag) timeline);
		} else if (timeline instanceof QuatAnimFlag) {
			return new TTanQuat((QuatAnimFlag) timeline);
		}
		return null;
	}

	public AnimFlag<T> getTimeline() {
		return timeline;
	}

	public void setFromKF2(int time, AnimFlag<T> timeline) {
		TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap();
		if (timeline.getEntryAt(time) != null && entryMap.lowerKey(time) != null && entryMap.higherKey(time) != null) {
			tang.setValues(timeline.getEntryAt(time));
			cur.setValues(timeline.getEntryAt(time));
			prev.setValues(timeline.getEntryAt(entryMap.lowerKey(time)));
			next.setValues(timeline.getEntryAt(entryMap.higherKey(time)));
		}
	}

	public void bezInterp(int currTime, Entry<T> itStart, Entry<T> itEnd) {
		float t = getTimeFactor(currTime, itStart.time, itEnd.time);
		float[] f = getBezF(t);

		calculateInterp(itStart, itEnd, f);
	}

	protected float[] getBezF(float t) {
		float[] f = new float[4];

		f[0] = (1 - t) * (1 - t) * (1 - t);
		f[1] = 3 * t * (1 - t) * (1 - t);
		f[2] = 3 * t * t * (1 - t);
		f[3] = t * t * t;
		return f;
	}

	public void spline(int currTime, Entry<T> itStart, Entry<T> itEnd) {
		float t = getTimeFactor(currTime, itStart.time, itEnd.time);
		float[] f = getSplineF(t);

		calculateInterp(itStart, itEnd, f);
	}

	protected float[] getSplineF(float t) {
		float[] f = new float[4];

//		float f1 = ((2 * t * t * t) - (3 * t * t)) + 1;
//		float f2 = (t * t * t) - (2 * t * t * t);
//		float f3 = (t * t * t) - (t * t);
//		float f4 = (-2 * t * t * t) + (3 * t * t);
//
//		float f1b = ((2 * t - 3) * (t * t)) + 1;
//		float f2b = (1 - 2) * (t * t * t);
//		float f3b = (t - 1) * (t * t);
//		float f4b = (3 - 2 * t) * (t * t);

		f[0] = ((2 * t * t * t) - (3 * t * t)) + 1;
		f[1] = (t * t * t) - (2 * t * t * t);
		f[2] = (t * t * t) - (t * t);
		f[3] = (-2 * t * t * t) + (3 * t * t);
		return f;
	}

	public float getTimeFactor(int currTime, Integer timeStart, Integer timeEnd) {
		return (currTime - timeStart) / (float) (timeEnd - timeStart);
	}

	public float[] getTCB(int i) {
		float[] g = new float[4];

		float contP = i * continuity;
		float biasP = i * bias;

		float contN = -contP;
		float biasN = -biasP;

		g[0] = (1 - tension) * (1 + contN) * (1 + biasP) * 0.5f;
		g[1] = (1 - tension) * (1 + contP) * (1 + biasN) * 0.5f;
		g[2] = (1 - tension) * (1 + contP) * (1 + biasP) * 0.5f;
		g[3] = (1 - tension) * (1 + contN) * (1 + biasN) * 0.5f;
		return g;
	}

	private void initTang(int count) {
	}

	public void calcSplineParameters() {
		float bMid = 0;
		float bStart = -1;
		float bEnd = 1;

		isLogsReady = false;

		while (Math.abs(bStart - bEnd) >= 0.0001) {
			bMid = ((bEnd - bStart) * 0.5f) + bStart;

			bias = bStart;
			float vStart = calcWithConstBias(tang.deepCopy());

			bias = bEnd;
			float vEnd = calcWithConstBias(tang.deepCopy());

			if (vStart < vEnd) {
				bEnd = bMid;
			} else {
				bStart = bMid;
			}
		}

		bias = bMid;
	}

	public abstract void calcDerivative();


	public float calcWithConstBias(Entry<T> tang2) {
		float cMin = 0;
		float tMin = 0;
		float cCur;
		float tCur;
		float delta;
		float cCurBeg = -1;
		float cCurEnd = 1;
		float tCurBeg = -1;
		float tCurEnd = 1;
		float step = 0.1f;

		float ds = 1e6f;

		while (step > 0.001) {
			cCur = cCurBeg;
			do {
				tCur = tCurBeg;
				do {
					continuity = cCur;
					tension = tCur;
					delta = getDelta(tang2);
					if (delta < ds) {
						ds = delta;
						cMin = cCur;
						tMin = tCur;
					}
					tCur += step;
				} while (tCur <= tCurEnd);
				cCur += step;
			} while (cCur <= cCurEnd);

			cCurBeg = Math.max(cMin - step, -1);
			cCurEnd = Math.min(cMin + step, 1);

			tCurBeg = Math.max(tMin - step, -1);
			tCurEnd = Math.min(tMin + step, 1);

			step = step * 0.1f;
		}

		continuity = cMin;
		tension = tMin;
		tang.set(tang2);
		return ds;
	}

	protected abstract float getDelta(Entry<T> tang2);

	public abstract void calculateInterp(Entry<T> itStart, Entry<T> itEnd, float[] f);

}
