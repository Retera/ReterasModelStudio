package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.MathUtils;
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

	public void calcDerivative(float tension, float continuity, float bias) {
		if (!isLogsReady) {

			qcur.set(cur.value);

			logNNP.set(cur.value).invertQuat().mul(next.value);
			calcLogQ(logNNP);

			logNMN.set(prev.value).mul(cur.value);
			calcLogQ(logNMN);

			isLogsReady = true;
		}
		if (calcTang.inTan == null) {
			calcTang.inTan = new Quat(0, 0, 0, 0);
			calcTang.outTan = new Quat(0, 0, 0, 0);
		} else {
			calcTang.inTan.setIdentity();
			calcTang.outTan.setIdentity();
		}

		float[] g = getTCBTangentFactors(-1, tension, continuity, bias);

		calcTang.outTan.addScaled(logNNP, g[2] - 1).addScaled(logNMN, g[3]).scale(.5f);
		calcTang.outTan.w = 0;
		calcExpQ(calcTang.outTan).mulLeft(qcur);

		calcTang.inTan.addScaled(logNNP, -g[0]).addScaled(logNMN, 1 - g[1]).scale(.5f);
		calcTang.inTan.w = calcTang.outTan.w;
		calcExpQ(calcTang.inTan).mulLeft(qcur);

	}

	private Quat calcLogQ(Quat q) {
		if (0.99999 < q.w) {
			q.w = 0.99999f;
		}
		float sinT = (float) (Math.acos(MathUtils.clamp(q.w, -1f, 1f)) / Math.sqrt(1 - (q.w * q.w)));
		q.scale(sinT);
		q.w = 0;
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


	@Override
	protected float getDelta(Entry<Quat> orgTangs, float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut.set(calcTang.outTan).sub(orgTangs.outTan);
		deltaIn.set(calcTang.inTan).sub(orgTangs.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z) + Math.abs(deltaOut.w)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z) + Math.abs(deltaIn.w);
	}

	@Override
	protected float getDelta(float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut.set(calcTang.outTan).sub(orgTangs.outTan);
		deltaIn.set(calcTang.inTan).sub(orgTangs.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z) + Math.abs(deltaOut.w)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z) + Math.abs(deltaIn.w);
	}
}
