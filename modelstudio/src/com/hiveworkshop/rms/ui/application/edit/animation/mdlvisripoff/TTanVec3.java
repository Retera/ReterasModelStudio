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
	public TTanVec3(AnimFlag<Vec3> timeline, Sequence anim) {
		super(timeline, anim);
		deltaOut = new Vec3();
		deltaIn = new Vec3();
		logNNP = new Vec3();
		logNMN = new Vec3();
	}

	public void calcDerivative(float tension, float continuity, float bias) {
		// Calculating the derivatives in point Cur (for count cells)
		if (calcTang.inTan == null) {
			calcTang.inTan = new Vec3(0, 0, 0);
			calcTang.outTan = new Vec3(0, 0, 0);
		}

		float[] g = getTCBTangentFactors(1, tension, continuity, bias);

		logNNP.set(cur.value).sub(prev.value);
		logNMN.set(next.value).sub(cur.value);

		calcTang.inTan.set(logNNP).scale(g[0]).addScaled(logNMN, g[1]);
		calcTang.outTan.set(logNNP).scale(g[2]).addScaled(logNMN, g[3]);

	}

	public Vec3 calculateInterp(Entry<Vec3> itStart, Entry<Vec3> itEnd, float[] f) {
		itEnd.value.scale(f[3]).addScaled(itStart.value, f[0]).addScaled(itStart.outTan, f[1]).addScaled(itEnd.inTan, f[2]);
		return itEnd.value;
	}

	@Override
	protected float getDelta(Entry<Vec3> orgTangs, float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut.set(calcTang.outTan).sub(orgTangs.outTan);
		deltaIn.set(calcTang.inTan).sub(orgTangs.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z);
	}
	@Override
	protected float getDelta(float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut.set(calcTang.outTan).sub(orgTangs.outTan);
		deltaIn.set(calcTang.inTan).sub(orgTangs.inTan);
		return Math.abs(deltaOut.x) + Math.abs(deltaOut.y) + Math.abs(deltaOut.z)
				+ Math.abs(deltaIn.x) + Math.abs(deltaIn.y) + Math.abs(deltaIn.z);
	}
}
