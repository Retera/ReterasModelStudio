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
	AnimFlag<T> timeline;
	public float bias;
	public float tension;
	public float continuity; // Spline parameters
	protected Entry<T> prev;
	public Entry<T> tang; // storage for tangents
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
		tang = new Entry<>(value);
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
				tang.setValues(timeline.getEntryAt(anim, time));
				cur.setValues(timeline.getEntryAt(anim, time));
				prev.setValues(timeline.getEntryAt(anim, prevTime));
				next.setValues(timeline.getEntryAt(anim, nextTime));
			}
		} else if (prevTime != null || nextTime != null) {
			tang.setTime(time);
			tang.setValue(timeline.interpolateAt(anim, time));
			tang.linearize();
			cur.setTime(time);
			cur.setValue(timeline.interpolateAt(anim, time));
			prev.setValue(timeline.interpolateAt(anim, (time-1 + anim.getLength())%anim.getLength()));
			next.setValue(timeline.interpolateAt(anim, (time+1 + anim.getLength())%anim.getLength()));
		}
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
		float bStart = -1;
		float bEnd = 1;

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
		float continuityMin = 0;
		float tensionMin = 0;
		float continuityCur;
		float tensionCur;
		float delta;
		float continuityCurBeg = -1;
		float continuityCurEnd = 1;
		float tensionCurBeg = -1;
		float tensionCurEnd = 1;

		float ds = 1e6f;

		orgTangs.setValues(tang);

		for (float step = 0.1f; 0.001 < step; step *= 0.1f) {
			continuityCur = continuityCurBeg;
			do {
				tensionCur = tensionCurBeg;
				do {
					delta = getDelta(orgTangs, tensionCur, continuityCur, bias);
					if (delta < ds) {
						ds = delta;
						continuityMin = continuityCur;
						tensionMin = tensionCur;
					}
					tensionCur += step;
				} while (tensionCur <= tensionCurEnd);
				continuityCur += step;
			} while (continuityCur <= continuityCurEnd);

			continuityCurBeg = Math.max(continuityMin - step, -1);
			continuityCurEnd = Math.min(continuityMin + step, 1);

			tensionCurBeg = Math.max(tensionMin - step, -1);
			tensionCurEnd = Math.min(tensionMin + step, 1);
		}

		continuity = continuityMin;
		tension = tensionMin;
		tang.setValues(orgTangs);
		return ds;
	}

	public float calcWithConstBias2(float bias) {
		float contMin = 0;
		float tensMin = 0;
		float contCurBeg = -1;
		float tensCurBeg = -1;
		float contCurEnd = 1;
		float tensCurEnd = 1;

		float ds = 1e6f;

		orgTangs.setValues(tang);

		for (float step = 0.1f; 0.001 < step; step *= 0.1f) {
			for (float cCur = contCurBeg; cCur <= contCurEnd; cCur += step) {
				for (float tCur = tensCurBeg; tCur <= tensCurEnd; tCur += step) {
					float delta = getDelta(orgTangs, tCur, cCur, bias);
					if (delta < ds) {
						ds = delta;
						contMin = cCur;
						tensMin = tCur;
					}
				}
			}

			contCurBeg = Math.max(contMin - step, -1);
			tensCurBeg = Math.max(tensMin - step, -1);

			contCurEnd = Math.min(contMin + step, 1);
			tensCurEnd = Math.min(tensMin + step, 1);
		}

		continuity = contMin;
		tension = tensMin;
		tang.setValues(orgTangs);
		return ds;
	}

	protected abstract float getDelta(Entry<T> tang2, float tension, float continuity, float bias);
	protected abstract float getDelta(float tension, float continuity, float bias);

	public abstract T calculateInterp(Entry<T> itStart, Entry<T> itEnd, float[] f);

	public float[] getTCB() {
		return new float[] {tension, continuity, bias};
	}

}
