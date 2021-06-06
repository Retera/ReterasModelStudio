package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;

import javax.swing.*;
import java.awt.*;

public class CurveRenderer extends JPanel {
	private final Entry entryD = new Entry(0, 0.0, 0.0, 0.0);
	private final Entry entryS = new Entry(0, 0.0, 0.0, 0.0);
	private TTan der;
	private AnimFlag timeline;

	public CurveRenderer(TTan der, AnimFlag timeline) {
		this.der = der;
		this.timeline = timeline;
	}

	public void setTTder(TTan newDer) {
		der = newDer;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Rectangle rect = getBounds();
		float pixPerUnitX = 0.005f * rect.width;
		float pixPerUnitY = rect.height / 130f;
		float renderX = rect.x, renderY = rect.y + rect.height;

		g.setColor(Color.BLUE);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);

		TTan.setObjToNewValue(der.prev.value, 0, 0);
		TTan.setObjToNewValue(der.cur.value, 0, 100);
		TTan.setObjToNewValue(der.next.value, 0, 0);
		der.calcDerivativeXD(1);
		g.setColor(Color.BLACK);
		InterpolationType interpType = timeline.getInterpolationType();

		for (int i = 0; i <= 100; i += 2) {
			entryD.set(der.tang);
			TTan.setObjToNewValue(entryD.value, 0, 100);
			entryD.time = 100;

			entryS.time = 0;
			TTan.setObjToNewValue(entryS.value, 0, 0);
			TTan.setObjToNewValue(entryS.inTan, 0, 0);
			TTan.setObjToNewValue(entryS.outTan, 0, 0);

			TTan_doStuff(i, interpType);
			float newRenderX = Math.round(pixPerUnitX * i);
			float newRenderY = rect.height - Math.round(pixPerUnitY * TTan.getSubValue(entryD.value, 0).floatValue());
			g.drawLine((int) renderX, (int) renderY, (int) newRenderX, (int) newRenderY);
			renderX = newRenderX;
			renderY = newRenderY;
		}

		// Second half of the Curve (Spline?)

		for (int i = 100; i <= 101; i += 2) {
			TTan.setObjToNewValue(entryD.value, 0, 0);
			entryD.time = 200;
			TTan.setObjToNewValue(entryD.inTan, 0, 0);
			TTan.setObjToNewValue(entryD.outTan, 0, 0);

			entryS.set(der.tang);
			entryS.time = 100;
			TTan.setObjToNewValue(entryS.value, 0, 100);

			TTan_doStuff(i, interpType);
			float newRenderX = Math.round(pixPerUnitX * i);
			float newRenderY = rect.height - Math.round(pixPerUnitY * TTan.getSubValue(entryD.value, 0).floatValue());
			g.drawLine((int) renderX, (int) renderY, (int) newRenderX, (int) newRenderY);
			renderX = newRenderX;
			renderY = newRenderY;
		}

		// Central line
		g.setColor(Color.RED);
		g.drawLine((Math.round(pixPerUnitX * 100)), rect.height, Math.round(pixPerUnitX * 100), rect.height - Math.round(pixPerUnitY * 100));
	}

	private void TTan_doStuff(int i, InterpolationType interpType) {
		switch (interpType) {
			case HERMITE -> TTan.spline(i, entryS, entryD);
			case BEZIER -> TTan.bezInterp(i, entryS, entryD);
		}
	}
}
