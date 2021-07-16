package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;

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
	public boolean isLogsReady; // "true" if Logarithms are ready

	public TTanFloat(AnimFlag<Float> timeline) {
		super(timeline);
		deltaOut = 0f;
		deltaIn = 0f;
	}

	public float getSubValue(Float value, int i) {
		return value;
	}

	public void calcDerivative() {
		// Calculating the derivatives in point Cur (for count cells)
		if (tang.inTan == null) {
			tang.inTan = 0f;
			tang.outTan = 0f;
		}

		float[] g = getTCB(1);

		float currPrev = cur.value - prev.value;
		float nextCurr = next.value - cur.value;

		tang.inTan = currPrev * g[0] + nextCurr * g[1];
		tang.outTan = currPrev * g[2] + nextCurr * g[3];
	}

	public void calculateInterp(Entry<Float> itStart, Entry<Float> itEnd, float[] f) {
		itEnd.value = itEnd.value * f[3] + itStart.value * f[0] + itStart.outTan * f[1] + itEnd.inTan * f[2];
	}

	@Override
	protected float getDelta(Entry<Float> tang2) {
		calcDerivative();
		deltaOut = tang.outTan - tang2.outTan;
		deltaIn = tang.inTan - tang2.inTan;
		return Math.abs(deltaOut) + Math.abs(deltaIn);
	}
}
