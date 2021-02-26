package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
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
	private float ds; // Deviation (for Calculation)
	private Quat qcur, logNNP, logNMN; // Logarithms and current quaternion
	public AnimFlag.Entry tang; // storage for tangents
	public float bias, tension, continuity; // Spline parameters
	public AnimFlag.Entry prev, cur, next; // Values in KK
	public boolean isLogsReady; // "true" if Logarithms are ready

	public void calcDerivativeXD(final int count) {
		float g1, g2, g3, g4;
		// Calculating the derivatives in point Cur (for count cells)
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
		g1 = (1 - tension) * (1 - continuity) * (1 + bias) * 0.5f;
		g2 = (1 - tension) * (1 + continuity) * (1 - bias) * 0.5f;
		g3 = (1 - tension) * (1 + continuity) * (1 + bias) * 0.5f;
		g4 = (1 - tension) * (1 - continuity) * (1 - bias) * 0.5f;
		for (int i = 0; i < count; i++) {
			tang.inTan = assignSubscript(tang.inTan, i,
					(g1 * (getSubscript(cur.value, i).floatValue() - getSubscript(prev.value, i).floatValue())) + (g2
							* (getSubscript(next.value, i).floatValue() - getSubscript(cur.value, i).floatValue())));
			tang.outTan = assignSubscript(tang.outTan, i,
					(g3 * (getSubscript(cur.value, i).floatValue() - getSubscript(prev.value, i).floatValue())) + (g4
							* (getSubscript(next.value, i).floatValue() - getSubscript(cur.value, i).floatValue())));
		}
	}

	public void calcWithConstBias(final boolean isQuaternion, final int count) {
		float cMin, tMin, cCur, tCur, delta, cCurBeg, cCurEnd, tCurBeg, tCurEnd, step;
		AnimFlag.Entry tang2;

		tang2 = tang;
		cMin = 0;
		tMin = 0;
		ds = 1e6f;
		step = 0.1f;
		tCurBeg = -1;
		tCurEnd = 1;
		cCurBeg = -1;
		cCurEnd = 1;
		while (step > 0.001) {
			cCur = cCurBeg;
			do {
				tCur = tCurBeg;
				do {
					continuity = cCur;
					tension = tCur;
					if (isQuaternion) {
						calcDerivative4D();
						delta = Math.abs(
								getSubscript(tang.outTan, 0).floatValue() - getSubscript(tang2.outTan, 0).floatValue())
								+ Math.abs(getSubscript(tang.outTan, 1).floatValue()
										- getSubscript(tang2.outTan, 1).floatValue())
								+ Math.abs(getSubscript(tang.outTan, 2).floatValue()
										- getSubscript(tang2.outTan, 2).floatValue())
								+ Math.abs(getSubscript(tang.outTan, 3).floatValue()
										- getSubscript(tang2.outTan, 3).floatValue())
								+ Math.abs(getSubscript(tang.inTan, 0).floatValue()
										- getSubscript(tang2.inTan, 0).floatValue())
								+ Math.abs(getSubscript(tang.inTan, 1).floatValue()
										- getSubscript(tang2.inTan, 1).floatValue())
								+ Math.abs(getSubscript(tang.inTan, 2).floatValue()
										- getSubscript(tang2.inTan, 2).floatValue())
								+ Math.abs(getSubscript(tang.inTan, 3).floatValue()
										- getSubscript(tang2.inTan, 3).floatValue());
					} else {
						calcDerivativeXD(count);
						delta = 0;
						for (int i = 0; i < count; i++) {
							delta += Math
									.abs(getSubscript(tang.outTan, i).floatValue()
											- getSubscript(tang2.outTan, i).floatValue())
									+ Math.abs(getSubscript(tang.inTan, i).floatValue()
											- getSubscript(tang2.inTan, i).floatValue());
						}
					}
					if (delta < ds) {
						ds = delta;
						cMin = cCur;
						tMin = tCur;
					}
					tCur += step;
				} while (tCur <= tCurEnd);
				cCur += step;
			} while (cCur <= cCurEnd);
			cCurBeg = cMin - step;
			if (cCurBeg < -1) {
				cCurBeg = -1;
			}
			cCurEnd = cMin + step;
			if (cCurEnd > 1) {
				cCurEnd = 1;
			}
			tCurBeg = tMin - step;
			if (tCurBeg < -1) {
				tCurBeg = -1;
			}
			tCurEnd = tMin + step;
			if (tCurEnd > 1) {
				tCurEnd = 1;
			}
			step = step * 0.1f;
		}

		continuity = cMin;
		tension = tMin;
		tang = tang2;
	}

	public void calcSplineParameters(final boolean isQuaternion, final int count) {
		float bMid, bStart, bEnd, vStart, vEnd, delta = 0;
		bStart = -1;
		bEnd = 1;
		isLogsReady = false;
		do {
			bMid = ((bEnd - bStart) * 0.5f) + bStart;

			bias = bStart;
			calcWithConstBias(isQuaternion, count);
			vStart = ds;

			bias = bEnd;
			calcWithConstBias(isQuaternion, count);
			vEnd = ds;

			if (vStart < vEnd) {
				bEnd = bMid;
			} else {
				bStart = bMid;
			}
			delta = Math.abs(bStart - bEnd);
		} while (delta >= 0.0001);
		bias = bMid;
	}

	public void calcDerivative4D() {
		final Quat qprev = new Quat(0, 0, 0, 0), qnext = new Quat(0, 0, 0, 0),
				q = new Quat(0, 0, 0, 0);
		final float g1, g2, g3, g4;
		if (!isLogsReady) {
			qcur.x = getSubscript(cur.value, 0).floatValue();
			qcur.y = getSubscript(cur.value, 1).floatValue();
			qcur.z = getSubscript(cur.value, 2).floatValue();
			qcur.w = getSubscript(cur.value, 3).floatValue();

			qprev.x = getSubscript(prev.value, 0).floatValue();
			qprev.y = getSubscript(prev.value, 1).floatValue();
			qprev.z = getSubscript(prev.value, 2).floatValue();
			qprev.w = getSubscript(prev.value, 3).floatValue();

			qnext.x = getSubscript(next.value, 0).floatValue();
			qnext.y = getSubscript(next.value, 1).floatValue();
			qnext.z = getSubscript(next.value, 2).floatValue();
			qnext.w = getSubscript(next.value, 3).floatValue();

			getInverseQuaternion(qcur, logNNP);
			mulQuaternions(logNNP, qnext, logNNP);
			calcLogQ(logNNP);
			getInverseQuaternion(qprev, logNMN);
			mulQuaternions(logNMN, qcur, logNMN);
			calcLogQ(logNMN);
			isLogsReady = true;
		}
		if (tang.inTan == null) {
			tang.inTan = new Quat(0, 0, 0, 0);
			tang.outTan = new Quat(0, 0, 0, 0);
		}
		g1 = (1 - tension) * (1 + continuity) * (1 - bias) * 0.5f;
		g2 = (1 - tension) * (1 - continuity) * (1 + bias) * 0.5f;
		g3 = (1 - tension) * (1 - continuity) * (1 - bias) * 0.5f;
		g4 = (1 - tension) * (1 + continuity) * (1 + bias) * 0.5f;
		((Quat) tang.inTan).x = (g1 * logNNP.x) + (g2 * logNMN.x);
		((Quat) tang.outTan).x = (g3 * logNNP.x) + (g4 * logNMN.x);
		((Quat) tang.inTan).y = (g1 * logNNP.y) + (g2 * logNMN.y);
		((Quat) tang.outTan).y = (g3 * logNNP.y) + (g4 * logNMN.y);
		((Quat) tang.inTan).z = (g1 * logNNP.z) + (g2 * logNMN.z);
		((Quat) tang.outTan).z = (g3 * logNNP.z) + (g4 * logNMN.z);

		q.x = 0.5f * (((Quat) tang.outTan).x - logNNP.x);
		q.y = 0.5f * (((Quat) tang.outTan).y - logNNP.y);
		q.z = 0.5f * (((Quat) tang.outTan).z - logNNP.z);
		calcExpQ(q);
		mulQuaternions(qcur, q, q);
		((Quat) tang.outTan).x = q.x;
		((Quat) tang.outTan).y = q.y;
		((Quat) tang.outTan).z = q.z;
		((Quat) tang.outTan).w = q.w;

		q.x = 0.5f * (logNMN.x - ((Quat) tang.inTan).x);
		q.y = 0.5f * (logNMN.y - ((Quat) tang.inTan).y);
		q.z = 0.5f * (logNMN.z - ((Quat) tang.inTan).z);
		calcExpQ(q);
		mulQuaternions(qcur, q, q);
		((Quat) tang.inTan).x = q.x;
		((Quat) tang.inTan).y = q.y;
		((Quat) tang.inTan).z = q.z;
		((Quat) tang.inTan).w = q.w;

	}

	public static Object assignSubscript(final Object value, final int index, final Object newValue) {
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

	public static Number getSubscript(final Object value, final int index) {
		if ((value instanceof Number)) {
			return (Number) value;
		} else if (value instanceof Vec3) {
			return ((Vec3) value).getCoord((byte) index);
		} else if (value instanceof Quat) {
			return ((Quat) value).getCoord((byte) index);
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + value + ", " + index);
	}

	private static void getInverseQuaternion(final Quat qsrc, final Quat qdest) {
		float bigN;
		bigN = 1 / ((qsrc.x * qsrc.x) + (qsrc.y * qsrc.y) + (qsrc.z * qsrc.z) + (qsrc.w * qsrc.w));
		qdest.x = -qsrc.x * bigN;
		qdest.y = -qsrc.y * bigN;
		qdest.z = -qsrc.z * bigN;
		qdest.w = qsrc.w * bigN;
	}

	private static void mulQuaternions(final Quat q1, final Quat q2,
			final Quat qdest) {
		float a, b, c, d, e, f, g, h;
		a = (q1.w + q1.x) * (q2.w + q2.x);
		b = (q1.z - q1.y) * (q2.y - q2.z);
		c = (q1.x - q1.w) * (q2.y + q2.z);
		d = (q1.y + q1.z) * (q2.x - q2.w);
		e = (q1.x + q1.z) * (q2.x + q2.y);
		f = (q1.x - q1.z) * (q2.x - q2.y);
		g = (q1.w + q1.y) * (q2.w - q2.z);
		h = (q1.w - q1.y) * (q2.w + q2.z);

		qdest.w = b + (((-e - f) + g + h) * 0.5f);
		qdest.x = a - ((e + f + g + h) * 0.5f);
		qdest.y = -c + ((((e - f) + g) - h) * 0.5f);
		qdest.z = -d + (((e - f - g) + h) * 0.5f);
	}

	private void calcLogQ(final Quat q) {
		float sint;
		if (q.w > 0.99999) {
			q.w = 0.99999f;
		}
		sint = (float) (Math.acos(q.w) / Math.sqrt(1 - (q.w * q.w)));
		q.x = q.x * sint;
		q.y = q.y * sint;
		q.z = q.z * sint;
		q.w = 0;
	}

	private void calcExpQ(final Quat q) {
		float t, divt;
		t = (float) Math.sqrt((q.x * q.x) + (q.y * q.y) + (q.z * q.z));
		if (t < 1e-5) {
			q.x = 1;
			q.y = 0;
			q.z = 0;
			q.w = 0;
			return;
		}
		divt = (float) Math.sin(t) / t;
		q.w = (float) Math.cos(t);
		q.x = q.x * divt;
		q.y = q.y * divt;
		q.z = q.z * divt;
	}

	public static void bezInterp(final int frame, final AnimFlag.Entry its, final AnimFlag.Entry itd) {
		float t, f1, f2, f3, f4;
		t = (frame - its.time) / (float) (itd.time - its.time);

		f1 = (1 - t) * (1 - t) * (1 - t);
		f2 = 3 * t * (1 - t) * (1 - t);
		f3 = 3 * t * t * (1 - t);
		f4 = t * t * t;

		for (int i = 0; i < 3; i++) {
			assignSubscript(itd.value, i,
					(f1 * getSubscript(its.value, i).floatValue()) + (f2 * getSubscript(its.outTan, i).floatValue())
							+ (f3 * getSubscript(itd.inTan, i).floatValue())
							+ (f4 * getSubscript(itd.value, i).floatValue()));
		}

	}

	public static void spline(final int frame, final AnimFlag.Entry its, final AnimFlag.Entry itd) {
		float t, f1, f2, f3, f4;
		t = (frame - its.time) / (float) (itd.time - its.time);

		f1 = ((2 * t * t * t) - (3 * t * t)) + 1;
		f2 = (t * t * t) - (2 * t * t * t);
		f3 = (-2 * t * t * t) + (3 * t * t);
		f4 = (t * t * t) - (t * t);

		for (int i = 0; i < 4; i++) {
			assignSubscript(itd.value, i,
					(f1 * getSubscript(its.value, i).floatValue()) + (f2 * getSubscript(its.outTan, i).floatValue())
							+ (f3 * getSubscript(itd.value, i).floatValue())
							+ (f4 * getSubscript(itd.inTan, i).floatValue()));
		}

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
