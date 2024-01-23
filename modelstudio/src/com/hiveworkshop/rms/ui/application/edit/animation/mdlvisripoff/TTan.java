package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

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
	private static final float T_MIN = -1;
	private static final float T_MAX =  1;
	private static final float C_MIN = -1;
	private static final float C_MAX =  1;
	private static final float B_MIN = -1;
	private static final float B_MAX =  1;
	private final AnimFlag<T> timeline;
	public float bias;
	public float tension;
	public float continuity; // Spline parameters
	protected Entry<T> prev;
	protected Entry<T> calcTang; // storage for tangents
	protected Entry<T> orgTangs; // storage for tangents
	public Entry<T> cur;
	protected Entry<T> next; // Values in KK
	protected T deltaOut;
	protected T deltaIn;
	protected T logNNP;
	protected T logNMN;

	public boolean isLogsReady; // "true" if Logarithms are ready

	public TTan(AnimFlag<T> timeline, Sequence anim) {
		this.timeline = timeline;
		Entry<T> value = timeline.getEntryMap(anim).firstEntry().getValue();
		calcTang = new Entry<>(value);
		orgTangs = new Entry<>(value);
		cur = new Entry<>(value);
		prev = new Entry<>(value);
		next = new Entry<>(value);
	}

	public static <Q> TTan<Q> getNewTTan(AnimFlag<Q> timeline, Sequence anim) {
		if (timeline instanceof FloatAnimFlag) {
			return (TTan<Q>) new TTanFloat((FloatAnimFlag) timeline, anim);
		} else if (timeline instanceof Vec3AnimFlag) {
			return (TTan<Q>) new TTanVec3((Vec3AnimFlag) timeline, anim);
		} else if (timeline instanceof QuatAnimFlag) {
			return (TTan<Q>) new TTanQuat((QuatAnimFlag) timeline, anim);
		}
		return null;
	}

	public AnimFlag<T> getTimeline() {
		return timeline;
	}

	public void setFromKF(int time, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap(anim);
		Integer prevTime = entryMap.lowerKey(time);
		Integer nextTime = entryMap.higherKey(time);
		if (timeline.getEntryAt(anim, time) != null) {
			if (prevTime == null) {
				prevTime = entryMap.lastKey();
			}
			if (nextTime == null) {
				nextTime = entryMap.firstKey();
			}
			if (prevTime != null && nextTime != null) {
				orgTangs.setValues(timeline.getEntryAt(anim, time));
				cur.setValues(timeline.getEntryAt(anim, time));
				prev.setValues(timeline.getEntryAt(anim, prevTime));
				next.setValues(timeline.getEntryAt(anim, nextTime));
			}
		} else if (prevTime != null || nextTime != null) {
			orgTangs.setTime(time);
			orgTangs.setValue(timeline.interpolateAt(anim, time));
			cur.setTime(time);
			cur.setValue(timeline.interpolateAt(anim, time));
			prev.setValue(timeline.interpolateAt(anim, (time-1 + anim.getLength())%anim.getLength()));
			next.setValue(timeline.interpolateAt(anim, (time+1 + anim.getLength())%anim.getLength()));

			orgTangs.unLinearize();
		}
		calcTang.setValues(orgTangs);
	}

	public T bezInterp(int currTime, Entry<T> itStart, Entry<T> itEnd) {
		float t = getTimeFactor(currTime, itStart.time, itEnd.time);
		float[] f = getBezF(t);

		return calculateInterp(itStart, itEnd, f);
	}

	protected float[] getBezF(float t) {
		float[] f = new float[4];

		f[0] = (1 - t) * (1 - t) * (1 - t);
		f[1] = 3 * t * (1 - t) * (1 - t);
		f[2] = 3 * t * t * (1 - t);
		f[3] = t * t * t;
		return f;
	}

	public T hermiteInterp(int currTime, Entry<T> itStart, Entry<T> itEnd) {
		float t = getTimeFactor(currTime, itStart.time, itEnd.time);
		float[] f = getSplineF(t);

		return calculateInterp(itStart, itEnd, f);

//		itEnd.value.scale(f[3]).addScaled(itStart.value, f[0]).addScaled(itStart.outTan, f[1]).addScaled(itEnd.inTan, f[2]);
	}

	public void printTCB() {
		System.out.println("t: " + tension + ", c: " + continuity + ", b: " + bias);
	}

	protected float[] getSplineF(float t) {
		float[] f = new float[4];

//		float[] fb = new float[4];
//		fb[0] = ((2 * t - 3) * (t * t)) + 1;
//		fb[1] = (1 - 2) * (t * t * t);
//		fb[2] = (t - 1) * (t * t);
//		fb[3] = (3 - 2 * t) * (t * t);

		f[0] = ((2 * t * t * t) - (3 * t * t)) + 1;
		f[1] = (t * t * t) - (2 * t * t * t);
		f[2] = (t * t * t) - (t * t);
		f[3] = (-2 * t * t * t) + (3 * t * t);
		return f;
	}

	public float getTimeFactor(int time, Integer timeStart, Integer timeEnd) {
		return (time - timeStart) / (float) (timeEnd - timeStart);
	}

	public void setTBC(float tension, float continuity, float bias) {
		this.tension = tension;
		this.continuity = continuity;
		this.bias = bias;
	}

	public float[] getTCBTangentFactors(int negForQuat, float tension, float continuity, float bias) {
		float[] g = new float[4];

		float contP = negForQuat * continuity;
		float biasP = negForQuat * bias;

		float contN = -contP;
		float biasN = -biasP;

		g[0] = (1 - tension) * (1 + contN) * (1 + biasP) * 0.5f;
		g[1] = (1 - tension) * (1 + contP) * (1 + biasN) * 0.5f;
		g[2] = (1 - tension) * (1 + contP) * (1 + biasP) * 0.5f;
		g[3] = (1 - tension) * (1 + contN) * (1 + biasN) * 0.5f;
		return g;
	}

	public void calcSplineParameters() {
		float bMid = 0;
		float bStart = B_MIN;
		float bEnd = B_MAX;

		isLogsReady = false;

		while (0.0001 <= Math.abs(bStart - bEnd)) {
			bMid = ((bEnd - bStart) * 0.5f) + bStart;

			float vStart = calcWithConstBias(bStart);

			float vEnd = calcWithConstBias(bEnd);

			if (vStart < vEnd) {
				bEnd = bMid;
			} else {
				bStart = bMid;
			}
		}

		bias = bMid;
	}

	public void calcDerivative() {
		calcDerivative(tension, continuity, bias);
	}

	public abstract void calcDerivative(float tension, float continuity, float bias);

	public float calcWithConstBias(float bias) {
		float contMinFound = 0;
		float tensMinFound = 0;
		float contMinLim = C_MIN;
		float contMaxLim = C_MAX;
		float tensMinLim = T_MIN;
		float tensMaxLim = T_MAX;

		float ds = 1e6f;
		calcTang.setValues(orgTangs);

		for (float step = 0.1f; 0.001 < step; step *= 0.1f) {
			for (float continuityCur = contMinLim; continuityCur <= contMaxLim; continuityCur += step) {
				for (float tensionCur = tensMinLim; tensionCur <= tensMaxLim; tensionCur += step) {
					float delta = getDelta(orgTangs, tensionCur, continuityCur, bias);
					if (delta < ds) {
						ds = delta;
						contMinFound = continuityCur;
						tensMinFound = tensionCur;
					}
				}
			}

			contMinLim = Math.max(contMinFound - step, C_MIN);
			contMaxLim = Math.min(contMinFound + step, C_MAX);

			tensMinLim = Math.max(tensMinFound - step, T_MIN);
			tensMaxLim = Math.min(tensMinFound + step, T_MAX);
		}

		continuity = contMinFound;
		tension = tensMinFound;
		return ds;
	}

	protected abstract float getDelta(Entry<T> orgTangs, float tension, float continuity, float bias);
	protected abstract float getDelta(float tension, float continuity, float bias);

	public abstract T calculateInterp(Entry<T> itStart, Entry<T> itEnd, float[] f);

	public float[] getTCB() {
		return new float[] {tension, continuity, bias};
	}

}
