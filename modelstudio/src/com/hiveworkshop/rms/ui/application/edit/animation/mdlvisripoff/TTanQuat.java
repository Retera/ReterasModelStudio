package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;

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
public class TTanQuat extends TTan<Quat> {
	private Quat qcur = new Quat();
//	private Quat logNNP = new Quat();
//	private Quat logNMN = new Quat(); // Logarithms and current quaternion

	public TTanQuat(AnimFlag<Quat> timeline, Sequence anim) {
		super(timeline, anim);
		deltaOut = new Quat();
		deltaIn = new Quat();
		logNNP = new Quat();
		logNMN = new Quat();
	}

	public void calcDerivative() {
		if (!isLogsReady) {

			qcur.set(cur.value);

			logNNP.set(cur.value).invertQuat().mul(next.value);
			calcLogQ(logNNP);

			logNMN.set(prev.value).mul(cur.value);
			calcLogQ(logNMN);

			isLogsReady = true;
		}
		if (tang.inTan == null) {
			tang.inTan = new Quat(0, 0, 0, 0);
			tang.outTan = new Quat(0, 0, 0, 0);
		}

		float[] g = getTCBTangentFactors(-1);

		tang.inTan.set(logNNP).scale(g[0]).addScaled(logNMN, g[1]);
		tang.outTan.set(logNNP).scale(g[2]).addScaled(logNMN, g[3]);

		Quat q = getQuat(0, tang.outTan, logNNP, qcur);
		tang.outTan.set(q);

		Quat q2 = getQuat(q.w, logNMN, tang.inTan, qcur);
		tang.inTan.set(q2);

	}

	private Quat calcLogQ(Quat q) {
		if (q.w > 0.99999) {
			q.w = 0.99999f;
		}
		float sinT = (float) (Math.acos(q.w) / Math.sqrt(1 - (q.w * q.w)));
		q.scale(sinT);
		q.w = 0;
		return q;
	}

	private Quat getQuat(float w, Quat quat1, Quat quat2, Quat qcur) {
		Quat q = new Quat(quat1);
		q.sub(quat2).scale(.5f);
		q.w = w;
		calcExpQ(q).mulLeft(qcur);
		return q;
	}

	private Quat calcExpQ(Quat q) {
		float t = q.length();
		if (t < 1e-5) {
			return (Quat) q.set(1, 0, 0, 0);
		}

		q.scale((float) Math.sin(t) / t);
		q.w = (float) Math.cos(t);
		return q;
	}

	public Quat calculateInterp(Entry<Quat> itStart, Entry<Quat> itEnd, float[] f) {
		itEnd.value.scale(f[3]).addScaled(itStart.value, f[0]).addScaled(itStart.outTan, f[1]).addScaled(itEnd.inTan, f[2]);
		return itEnd.value;
	}

//	private Quat getQuat(float w, Quat quat1, Quat quat2, Quat qcur) {
//		Quat q = new Quat(0, 0, 0, w);
//
//		q.x = 0.5f * (quat1.x - quat2.x);
//		q.y = 0.5f * (quat1.y - quat2.y);
//		q.z = 0.5f * (quat1.z - quat2.z);
//		calcExpQ(q);
//		q.mulLeft(qcur);
//		return q;
//	}
//
//	private Quat calcLogQ(Quat q) {
//		if (q.w > 0.99999) {
//			q.w = 0.99999f;
//		}
//		float sinT = (float) (Math.acos(q.w) / Math.sqrt(1 - (q.w * q.w)));
//		q.x = q.x * sinT;
//		q.y = q.y * sinT;
//		q.z = q.z * sinT;
//		q.w = 0;
//		return q;
//	}
//
//	private Quat calcExpQ(Quat q) {
//		float t = q.length();
//		if (t < 1e-5) {
//			return (Quat) q.set(1,0,0,0);
//		}
//		float divt = (float) Math.sin(t) / t;
//
//		q.x = q.x * divt;
//		q.y = q.y * divt;
//		q.z = q.z * divt;
//		q.w = (float) Math.cos(t);
//		return q;
//	}

	@Override
	protected float getDelta(Entry<Quat> tang2) {
		calcDerivative();
		deltaOut.set(tang.outTan).sub(tang2.outTan);
		deltaIn.set(tang.inTan).sub(tang2.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z) + Math.abs(deltaOut.w)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z) + Math.abs(deltaIn.w);
	}
}
