package com.hiveworkshop.wc3.gui.animedit.mdlvisripoff;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;

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
	private QuaternionRotation qcur, logNNP, logNMN; // Logarithms and current quaternion
	public AnimFlag.Entry tang; // storage for tangents
	public float bias, tension, continuity; // Spline parameters
	public AnimFlag.Entry prev, cur, next; // Values in KK
	public boolean isLogsReady; // "true" if Logarithms are ready

	public void calcDerivativeXD(final int count) {
		float g1, g2, g3, g4;
		// Calculating the derivatives in point Cur (for count cells)
		if (tang.inTan == null) {
			switch (count) {
			case 1:
				tang.inTan = Double.valueOf(0);
				tang.outTan = Double.valueOf(0);
				break;
			case 3:
				tang.inTan = new Vertex(0, 0, 0);
				tang.outTan = new Vertex(0, 0, 0);
				break;
			case 4:
				tang.inTan = new QuaternionRotation(0, 0, 0, 0);
				tang.outTan = new QuaternionRotation(0, 0, 0, 0);
				break;
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
		final QuaternionRotation qprev = new QuaternionRotation(0, 0, 0, 0), qnext = new QuaternionRotation(0, 0, 0, 0),
				q = new QuaternionRotation(0, 0, 0, 0);
		final float g1, g2, g3, g4;
		if (!isLogsReady) {
			qcur.a = getSubscript(cur.value, 0).doubleValue();
			qcur.b = getSubscript(cur.value, 1).doubleValue();
			qcur.c = getSubscript(cur.value, 2).doubleValue();
			qcur.d = getSubscript(cur.value, 3).doubleValue();

			qprev.a = getSubscript(prev.value, 0).doubleValue();
			qprev.b = getSubscript(prev.value, 1).doubleValue();
			qprev.c = getSubscript(prev.value, 2).doubleValue();
			qprev.d = getSubscript(prev.value, 3).doubleValue();

			qnext.a = getSubscript(next.value, 0).doubleValue();
			qnext.b = getSubscript(next.value, 1).doubleValue();
			qnext.c = getSubscript(next.value, 2).doubleValue();
			qnext.d = getSubscript(next.value, 3).doubleValue();

			getInverseQuaternion(qcur, logNNP);
			mulQuaternions(logNNP, qnext, logNNP);
			calcLogQ(logNNP);
			getInverseQuaternion(qprev, logNMN);
			mulQuaternions(logNMN, qcur, logNMN);
			calcLogQ(logNMN);
			isLogsReady = true;
		}
		if (tang.inTan == null) {
			tang.inTan = new QuaternionRotation(0, 0, 0, 0);
			tang.outTan = new QuaternionRotation(0, 0, 0, 0);
		}
		g1 = (1 - tension) * (1 + continuity) * (1 - bias) * 0.5f;
		g2 = (1 - tension) * (1 - continuity) * (1 + bias) * 0.5f;
		g3 = (1 - tension) * (1 - continuity) * (1 - bias) * 0.5f;
		g4 = (1 - tension) * (1 + continuity) * (1 + bias) * 0.5f;
		((QuaternionRotation) tang.inTan).a = (g1 * logNNP.a) + (g2 * logNMN.a);
		((QuaternionRotation) tang.outTan).a = (g3 * logNNP.a) + (g4 * logNMN.a);
		((QuaternionRotation) tang.inTan).b = (g1 * logNNP.b) + (g2 * logNMN.b);
		((QuaternionRotation) tang.outTan).b = (g3 * logNNP.b) + (g4 * logNMN.b);
		((QuaternionRotation) tang.inTan).c = (g1 * logNNP.c) + (g2 * logNMN.c);
		((QuaternionRotation) tang.outTan).c = (g3 * logNNP.c) + (g4 * logNMN.c);

		q.a = 0.5 * (((QuaternionRotation) tang.outTan).a - logNNP.a);
		q.b = 0.5 * (((QuaternionRotation) tang.outTan).b - logNNP.b);
		q.c = 0.5 * (((QuaternionRotation) tang.outTan).c - logNNP.c);
		calcExpQ(q);
		mulQuaternions(qcur, q, q);
		((QuaternionRotation) tang.outTan).a = q.a;
		((QuaternionRotation) tang.outTan).b = q.b;
		((QuaternionRotation) tang.outTan).c = q.c;
		((QuaternionRotation) tang.outTan).d = q.d;

		q.a = 0.5 * (logNMN.a - ((QuaternionRotation) tang.inTan).a);
		q.b = 0.5 * (logNMN.b - ((QuaternionRotation) tang.inTan).b);
		q.c = 0.5 * (logNMN.c - ((QuaternionRotation) tang.inTan).c);
		calcExpQ(q);
		mulQuaternions(qcur, q, q);
		((QuaternionRotation) tang.inTan).a = q.a;
		((QuaternionRotation) tang.inTan).b = q.b;
		((QuaternionRotation) tang.inTan).c = q.c;
		((QuaternionRotation) tang.inTan).d = q.d;

	}

	public static Object assignSubscript(final Object value, final int index, final Object newValue) {
		if ((value instanceof Double) || (value instanceof Integer)) {
			return newValue;
		} else if (value instanceof Vertex) {
			((Vertex) value).setCoord((byte) index, ((Number) newValue).doubleValue());
			return value;
		} else if (value instanceof QuaternionRotation) {
			((QuaternionRotation) value).setCoord((byte) index, ((Number) newValue).doubleValue());
			return value;
		}
		throw new IllegalArgumentException("Unknown subscripting (set): " + value + ", " + index + ", " + newValue);
	}

	public static Number getSubscript(final Object value, final int index) {
		if ((value instanceof Number)) {
			return (Number) value;
		} else if (value instanceof Vertex) {
			return ((Vertex) value).getCoord((byte) index);
		} else if (value instanceof QuaternionRotation) {
			return ((QuaternionRotation) value).getCoord((byte) index);
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + value + ", " + index);
	}

	private static void getInverseQuaternion(final QuaternionRotation qsrc, final QuaternionRotation qdest) {
		double bigN;
		bigN = 1 / ((qsrc.a * qsrc.a) + (qsrc.b * qsrc.b) + (qsrc.c * qsrc.c) + (qsrc.d * qsrc.d));
		qdest.a = -qsrc.a * bigN;
		qdest.b = -qsrc.b * bigN;
		qdest.c = -qsrc.c * bigN;
		qdest.d = qsrc.d * bigN;
	}

	private static void mulQuaternions(final QuaternionRotation q1, final QuaternionRotation q2,
			final QuaternionRotation qdest) {
		double a, b, c, d, e, f, g, h;
		a = (q1.d + q1.a) * (q2.d + q2.a);
		b = (q1.c - q1.b) * (q2.b - q2.c);
		c = (q1.a - q1.d) * (q2.b + q2.c);
		d = (q1.b + q1.c) * (q2.a - q2.d);
		e = (q1.a + q1.c) * (q2.a + q2.b);
		f = (q1.a - q1.c) * (q2.a - q2.b);
		g = (q1.d + q1.b) * (q2.d - q2.c);
		h = (q1.d - q1.b) * (q2.d + q2.c);

		qdest.d = b + (((-e - f) + g + h) * 0.5);
		qdest.a = a - ((e + f + g + h) * 0.5);
		qdest.b = -c + ((((e - f) + g) - h) * 0.5);
		qdest.c = -d + (((e - f - g) + h) * 0.5);
	}

	private void calcLogQ(final QuaternionRotation q) {
		double sint;
		if (q.d > 0.99999) {
			q.d = 0.99999;
		}
		sint = Math.acos(q.d) / Math.sqrt(1 - (q.d * q.d));
		q.a = q.a * sint;
		q.b = q.b * sint;
		q.c = q.c * sint;
		q.d = 0;
	}

	private void calcExpQ(final QuaternionRotation q) {
		double t, divt;
		t = Math.sqrt((q.a * q.a) + (q.b * q.b) + (q.c * q.c));
		if (t < 1e-5) {
			q.a = 1;
			q.b = 0;
			q.c = 0;
			q.d = 0;
			return;
		}
		divt = Math.sin(t) / t;
		q.d = Math.cos(t);
		q.a = q.a * divt;
		q.b = q.b * divt;
		q.c = q.c * divt;
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
		} else if (value instanceof Vertex) {
			return 3;
		} else if (value instanceof QuaternionRotation) {
			return 4;
		}
		return -1;
	}
}
