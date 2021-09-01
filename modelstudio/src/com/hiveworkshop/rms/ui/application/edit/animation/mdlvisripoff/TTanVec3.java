package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
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
 */
public class TTanVec3 extends TTan<Vec3> {
	public boolean isLogsReady; // "true" if Logarithms are ready

	public TTanVec3(AnimFlag<Vec3> timeline, Sequence anim) {
		super(timeline, anim);
		deltaOut = new Vec3();
		deltaIn = new Vec3();
		logNNP = new Vec3();
		logNMN = new Vec3();
	}

	public void setFromKF(int time, AnimFlag<Vec3> timeline) {

	}

	private void initTang(int count) {
		if (tang.inTan == null) {
			tang.inTan = new Vec3(0, 0, 0);
			tang.outTan = new Vec3(0, 0, 0);
		}
	}

	public float getSubValue(Vec3 value, int i) {
		return value.getCoord((byte) i);
	}

	public void calcDerivative() {
		// Calculating the derivatives in point Cur (for count cells)
		if (tang.inTan == null) {
			tang.inTan = new Vec3(0, 0, 0);
			tang.outTan = new Vec3(0, 0, 0);
		}

		float[] g = getTCB(1);

		logNNP.set(cur.value).sub(prev.value);
		logNMN.set(next.value).sub(cur.value);
//		Vec3 currPrev = new Vec3(cur.value).sub(prev.value);
//		Vec3 nextCurr = new Vec3(next.value).sub(cur.value);

		tang.inTan.set(logNNP).scale(g[0]).addScaled(logNMN, g[1]);
		tang.outTan.set(logNNP).scale(g[2]).addScaled(logNMN, g[3]);

	}

	public void calculateInterp(Entry<Vec3> itStart, Entry<Vec3> itEnd, float[] f) {
		itEnd.value.scale(f[3]).addScaled(itStart.value, f[0]).addScaled(itStart.outTan, f[1]).addScaled(itEnd.inTan, f[2]);
	}

	@Override
	protected float getDelta(Entry<Vec3> tang2) {
		calcDerivative();
		deltaOut.set(tang.outTan).sub(tang2.outTan);
		deltaIn.set(tang.inTan).sub(tang2.inTan);
//		Vec3 deltaOut = Vec3.getDiff(tang.outTan, tang2.outTan);
//		Vec3 deltaIn = Vec3.getDiff(tang.inTan, tang2.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z);
	}
}
