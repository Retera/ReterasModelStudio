package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

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
 *
 *
 */
public class TTan {
	public float bias;
	public float tension;
	public float continuity; // Spline parameters
	public Entry prev;
	public Entry tang; // storage for tangents
	public Entry cur;
	public Entry next; // Values in KK
	private float ds; // Deviation (for Calculation);
	private Quat qcur;
	private Quat logNNP;
	private Quat logNMN; // Logarithms and current quaternion
	public boolean isLogsReady; // "true" if Logarithms are ready

	public static void bezInterp(final int frame, final Entry its, final Entry itd) {
		float t, f1, f2, f3, f4;
		t = (frame - its.time) / (float) (itd.time - its.time);

		f1 = (1 - t) * (1 - t) * (1 - t);
		f2 = 3 * t * (1 - t) * (1 - t);
		f3 = 3 * t * t * (1 - t);
		f4 = t * t * t;

		for (int i = 0; i < 3; i++) {
			setObjToNewValue(itd.value, i,
					(f1 * getSubValue(its.value, i).floatValue())
							+ (f2 * getSubValue(its.outTan, i).floatValue())
							+ (f3 * getSubValue(itd.inTan, i).floatValue())
							+ (f4 * getSubValue(itd.value, i).floatValue()));
		}

	}

	public static Object setObjToNewValue(final Object value, final int index, final Object newValue) {
		if ((value instanceof Float) || (value instanceof Integer)) {
			return newValue;
		} else if (value instanceof Vec3) {
			((Vec3) value).setCoord((byte) index, ((Number) newValue).floatValue());
			return value;
		} else if (value instanceof Quat) {
			((Quat) value).setCoord((byte) index, ((Number) newValue).floatValue());
			return value;
		}
		throw new IllegalArgumentException("Unknown subscripting (set): " + value + ", " + index + ", " + newValue);
	}

	public static Number getSubValue(Object value, int index) {
		if ((value instanceof Number)) {
			return (Number) value;
		} else if (value instanceof Vec3) {
			return ((Vec3) value).getCoord((byte) index);
		} else if (value instanceof Quat) {
			return ((Quat) value).getCoord((byte) index);
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + value + ", " + index);
	}

	public static void spline(int frame, Entry its, Entry itd) {
		float t = (frame - its.time) / (float) (itd.time - its.time);

		float f1 = ((2 * t * t * t) - (3 * t * t)) + 1;
		float f2 = (t * t * t) - (2 * t * t * t);
		float f3 = (-2 * t * t * t) + (3 * t * t);
		float f4 = (t * t * t) - (t * t);

		for (int i = 0; i < 4; i++) {
			setObjToNewValue(itd.value, i,
					(f1 * getSubValue(its.value, i).floatValue())
							+ (f2 * getSubValue(its.outTan, i).floatValue())
							+ (f3 * getSubValue(itd.value, i).floatValue())
							+ (f4 * getSubValue(itd.inTan, i).floatValue()));
		}

	}

	private void initTang(int count) {
		if (tang.inTan == null) {
			switch (count) {
				case 1 -> {
					tang.inTan = (float) 0;
					tang.outTan = (float) 0;
				}
				case 3 -> {
					tang.inTan = new Vec3(0, 0, 0);
					tang.outTan = new Vec3(0, 0, 0);
				}
				case 4 -> {
					tang.inTan = new Quat(0, 0, 0, 0);
					tang.outTan = new Quat(0, 0, 0, 0);
				}
			}
		}
	}

	public void calcSplineParameters(final boolean isQuaternion, final int count) {
		float bMid = 0;
		float bStart = -1;
		float bEnd = 1;

		isLogsReady = false;
		do {
			bMid = ((bEnd - bStart) * 0.5f) + bStart;

			bias = bStart;
			calcWithConstBias(isQuaternion, count);
			float vStart = ds;

			bias = bEnd;
			calcWithConstBias(isQuaternion, count);
			float vEnd = ds;

			if (vStart < vEnd) {
				bEnd = bMid;
			} else {
				bStart = bMid;
			}

		} while (Math.abs(bStart - bEnd) >= 0.0001);

		bias = bMid;
	}

	public void calcDerivativeXD(final int count) {
		// Calculating the derivatives in point Cur (for count cells)
		initTang(count);
		float[] g = getTCB(1);

		for (int i = 0; i < count; i++) {
			float currIVal = getSubValue(cur.value, i).floatValue();
			float nextIval = getSubValue(next.value, i).floatValue();
			float prevIVal = getSubValue(prev.value, i).floatValue();

			float newInTanVal = (g[0] * (currIVal - prevIVal)) + (g[1] * (nextIval - currIVal));
			float newOutTanVal = (g[2] * (currIVal - prevIVal)) + (g[3] * (nextIval - currIVal));

			tang.inTan = setObjToNewValue(tang.inTan, i, newInTanVal);
			tang.outTan = setObjToNewValue(tang.outTan, i, newOutTanVal);
		}
	}

	public void calcDerivative3D() {
		// Calculating the derivatives in point Cur (for count cells)
		if (tang.inTan == null) {
			tang.inTan = new Vec3(0, 0, 0);
			tang.outTan = new Vec3(0, 0, 0);
		}

		float[] g = getTCB(1);

		Vec3 currPrev = new Vec3((Vec3) cur.value).sub((Vec3) prev.value);
		Vec3 nextCurr = new Vec3((Vec3) next.value).sub((Vec3) cur.value);

		Vec3 newInTan = new Vec3(currPrev).scale(g[0]).addScaled(nextCurr, g[1]);
		Vec3 newOutTan = new Vec3(currPrev).scale(g[2]).addScaled(nextCurr, g[3]);

		((Vec3) tang.inTan).set(newInTan);
		((Vec3) tang.outTan).set(newOutTan);
	}

	public void calcDerivative4D() {
		if (!isLogsReady) {
			Quat qprev = new Quat((Quat) prev.value);
			Quat qnext = new Quat((Quat) next.value);

			qcur.set((Quat) cur.value);

			logNNP.set(qcur).invertQuat();
			logNNP.set(logNNP).mul(qnext);

			calcLogQ(logNNP);
			logNMN.set(qprev).invertQuat();
			logNMN.set(logNMN).mul(qcur);


			calcLogQ(logNMN);
			isLogsReady = true;
		}
		if (tang.inTan == null) {
			tang.inTan = new Quat(0, 0, 0, 0);
			tang.outTan = new Quat(0, 0, 0, 0);
		}


		float[] g = getTCB(-1);

		((Quat) tang.inTan).x = (g[0] * logNNP.x) + (g[1] * logNMN.x);
		((Quat) tang.inTan).y = (g[0] * logNNP.y) + (g[1] * logNMN.y);
		((Quat) tang.inTan).z = (g[0] * logNNP.z) + (g[1] * logNMN.z);

		((Quat) tang.outTan).x = (g[2] * logNNP.x) + (g[3] * logNMN.x);
		((Quat) tang.outTan).y = (g[2] * logNNP.y) + (g[3] * logNMN.y);
		((Quat) tang.outTan).z = (g[2] * logNNP.z) + (g[3] * logNMN.z);

		Quat q = new Quat(0, 0, 0, 0);

		q.x = 0.5f * (((Quat) tang.outTan).x - logNNP.x);
		q.y = 0.5f * (((Quat) tang.outTan).y - logNNP.y);
		q.z = 0.5f * (((Quat) tang.outTan).z - logNNP.z);
		calcExpQ(q);
		q.mulLeft(qcur);

		((Quat) tang.outTan).set(q);

		q.x = 0.5f * (logNMN.x - ((Quat) tang.inTan).x);
		q.y = 0.5f * (logNMN.y - ((Quat) tang.inTan).y);
		q.z = 0.5f * (logNMN.z - ((Quat) tang.inTan).z);
		calcExpQ(q);
		q.mulLeft(qcur);

		((Quat) tang.inTan).set(q);
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

	private void calcLogQ(Quat q) {
		if (q.w > 0.99999) {
			q.w = 0.99999f;
		}
		float sint = (float) (Math.acos(q.w) / Math.sqrt(1 - (q.w * q.w)));
		q.x = q.x * sint;
		q.y = q.y * sint;
		q.z = q.z * sint;
		q.w = 0;
	}

	private Quat calcExpQ(Quat q) {
		float t = q.length();
		if (t < 1e-5) {
			return (Quat) q.set(1, 0, 0, 0);
		}
		float divt = (float) Math.sin(t) / t;

		q.w = (float) Math.cos(t);
		q.x = q.x * divt;
		q.y = q.y * divt;
		q.z = q.z * divt;
		return q;
	}

	public void calcWithConstBias(final boolean isQuaternion, final int count) {
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
		Entry tang2 = tang;

		ds = 1e6f;

		while (step > 0.001) {
			cCur = cCurBeg;
			do {
				tCur = tCurBeg;
				do {
					continuity = cCur;
					tension = tCur;
					delta = getDelta(isQuaternion, count, tang2);
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
		tang = tang2;
	}

	private float getDelta(boolean isQuaternion, int count, Entry tang2) {
		float delta;
		if (isQuaternion) {
			calcDerivative4D();
			delta = Math.abs(getSubValue(tang.outTan, 0).floatValue() - getSubValue(tang2.outTan, 0).floatValue())
					+ Math.abs(getSubValue(tang.outTan, 1).floatValue() - getSubValue(tang2.outTan, 1).floatValue())
					+ Math.abs(getSubValue(tang.outTan, 2).floatValue() - getSubValue(tang2.outTan, 2).floatValue())
					+ Math.abs(getSubValue(tang.outTan, 3).floatValue() - getSubValue(tang2.outTan, 3).floatValue())
					+ Math.abs(getSubValue(tang.inTan, 0).floatValue() - getSubValue(tang2.inTan, 0).floatValue())
					+ Math.abs(getSubValue(tang.inTan, 1).floatValue() - getSubValue(tang2.inTan, 1).floatValue())
					+ Math.abs(getSubValue(tang.inTan, 2).floatValue() - getSubValue(tang2.inTan, 2).floatValue())
					+ Math.abs(getSubValue(tang.inTan, 3).floatValue() - getSubValue(tang2.inTan, 3).floatValue());
		} else {
			calcDerivativeXD(count);
			delta = 0;
			for (int i = 0; i < count; i++) {
				delta += Math.abs(getSubValue(tang.outTan, i).floatValue() - getSubValue(tang2.outTan, i).floatValue())
						+ Math.abs(getSubValue(tang.inTan, i).floatValue() - getSubValue(tang2.inTan, i).floatValue());
			}
		}
		return delta;
	}

	public static int getSizeOfElement(final Object value) {
		if ((value instanceof Number)) {
			return 1;
		} else if (value instanceof Vec3) {
			return 3;
		} else if (value instanceof Quat) {
			return 4;
		}
		return -1;
	}
}
