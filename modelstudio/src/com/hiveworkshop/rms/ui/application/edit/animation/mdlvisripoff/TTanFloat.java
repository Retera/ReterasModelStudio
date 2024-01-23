package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

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
public class TTanFloat extends TTan<Float> {
	public TTanFloat(AnimFlag<Float> timeline, Sequence anim) {
		super(timeline, anim);
		deltaOut = 0f;
		deltaIn = 0f;
	}

	public void calcDerivative(float tension, float continuity, float bias) {
		// Calculating the derivatives in point Cur (for count cells)
		if (tang.inTan == null) {
			tang.inTan = 0f;
			tang.outTan = 0f;
		}

		float[] g = getTCBTangentFactors(1, tension, continuity, bias);

		float currPrev = cur.value - prev.value;
		float nextCurr = next.value - cur.value;

		tang.inTan = currPrev * g[0] + nextCurr * g[1];
		tang.outTan = currPrev * g[2] + nextCurr * g[3];
	}

	public Float calculateInterp(Entry<Float> itStart, Entry<Float> itEnd, float[] f) {
		itEnd.value = itEnd.value * f[3] + itStart.value * f[0] + itStart.outTan * f[1] + itEnd.inTan * f[2];
		return itEnd.value;
	}

	@Override
	protected float getDelta(Entry<Float> tang2, float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut = tang.outTan - tang2.outTan;
		deltaIn = tang.inTan - tang2.inTan;
		return Math.abs(deltaOut) + Math.abs(deltaIn);
	}
	@Override
	protected float getDelta(float tension, float continuity, float bias) {
		calcDerivative(tension, continuity, bias);
		deltaOut = tang.outTan - orgTangs.outTan;
		deltaIn = tang.inTan - orgTangs.inTan;
		return Math.abs(deltaOut) + Math.abs(deltaIn);
	}
}
