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
	private Entry<T> tang2; // storage for tangents
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
		tang2 = new Entry<>(value);
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

	public static TTan<?> getNewTTan2(AnimFlag<?> timeline, Sequence anim) {
		if (timeline instanceof FloatAnimFlag) {
			return new TTanFloat((FloatAnimFlag) timeline, anim);
		} else if (timeline instanceof Vec3AnimFlag) {
			return new TTanVec3((Vec3AnimFlag) timeline, anim);
		} else if (timeline instanceof QuatAnimFlag) {
			return new TTanQuat((QuatAnimFlag) timeline, anim);
		}
		return null;
	}

	public AnimFlag<T> getTimeline() {
		return timeline;
	}

	public void setFromKF(int time, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap(anim);
		if (timeline.getEntryAt(anim, time) != null && entryMap.lowerKey(time) != null && entryMap.higherKey(time) != null) {
			tang.setValues(timeline.getEntryAt(anim, time));
			cur.setValues(timeline.getEntryAt(anim, time));
			prev.setValues(timeline.getEntryAt(anim, entryMap.lowerKey(time)));
			next.setValues(timeline.getEntryAt(anim, entryMap.higherKey(time)));
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
//		itEnd.value.scale(f[3]).addScaled(itStart.value, f[0]).addScaled(itStart.outTan, f[1]).addScaled(itEnd.inTan, f[2]);
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

	public void setTBC(float tension, float continuity, float bias){
		this.tension = tension;
		this.continuity = continuity;
		this.bias = bias;
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

	public void calcSplineParameters() {
		float bMid = 0;
		float bStart = -1;
		float bEnd = 1;

		isLogsReady = false;

		while (Math.abs(bStart - bEnd) >= 0.0001) {
			bMid = ((bEnd - bStart) * 0.5f) + bStart;

			bias = bStart;
			float vStart = calcWithConstBias();

			bias = bEnd;
			float vEnd = calcWithConstBias();

			if (vStart < vEnd) {
				bEnd = bMid;
			} else {
				bStart = bMid;
			}
		}

		bias = bMid;
	}

	public abstract void calcDerivative();


	public float calcWithConstBias() {
		float cMin = 0;
		float tMin = 0;
		float cCur;
		float tCur;
		float delta;
		float cCurBeg = -1;
		float cCurEnd = 1;
		float tCurBeg = -1;
		float tCurEnd = 1;

		float ds = 1e6f;

		tang2.setValues(tang);

		for(float step = 0.1f; step>0.001; step *=0.1f){
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
		}

		continuity = cMin;
		tension = tMin;
		tang.setValues(tang2);
		return ds;
	}
	public float calcWithConstBias2() {
		float contMin = 0;
		float tensMin = 0;
		float contCurBeg = -1;
		float tensCurBeg = -1;
		float contCurEnd = 1;
		float tensCurEnd = 1;

		float ds = 1e6f;

		tang2.setValues(tang);

		for(float step = 0.1f; step>0.001; step *=0.1f){
			for(float cCur = contCurBeg; cCur <= contCurEnd; cCur += step){
				for(float tCur = tensCurBeg; tCur <= tensCurEnd; tCur += step){
					continuity = cCur;
					tension = tCur;
					float delta = getDelta(tang2);
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
		tang.setValues(tang2);
		return ds;
	}

	protected abstract float getDelta(Entry<T> tang2);

	public abstract void calculateInterp(Entry<T> itStart, Entry<T> itEnd, float[] f);

}
