package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;

public class CurveRenderer extends JPanel {
	SplineTracker<?> splineTracker;
	Vec2 renderStartPoint = new Vec2();
	Vec2 renderEndPoint = new Vec2();
	float pixPerUnitX;
	float pixPerUnitY;

	public CurveRenderer() {
	}

	public void setSplineTracker(SplineTracker<?> splineTracker) {
		this.splineTracker = splineTracker;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Rectangle rect = getBounds();
		int height = rect.height;
		pixPerUnitX = 0.005f * rect.width;
		pixPerUnitY = height / 130f;
		renderStartPoint.set(rect.x, rect.y + height);

		g.setColor(Color.BLUE);
		g.drawRect(rect.x, rect.y, rect.width, height);

		g.setColor(Color.BLACK);

		if (splineTracker != null && splineTracker.hasDer()) {
			splineTracker.prepareTTan();

			drawFirstSpline(g);
			drawSecondSpline(g);
		}

		// Central line
		g.setColor(Color.RED);

		int x1 = getEndX(100);
		g.drawLine(x1, height, x1, height - Math.round(pixPerUnitY * 100));
	}

	private void drawFirstSpline(Graphics g) {
		for (int i = 0; i <= 100; i += 2) {
			splineTracker.calcSplineStepStart(0);

			splineTracker.interpolate(i);
			drawSplineLine(g, i);
		}
	}

	private void drawSecondSpline(Graphics g) {
//		for (int i = 100; i <= 101; i += 2) {
		for (int i = 100; i <= 200; i += 2) {
			splineTracker.calcSplineStepEnd(200);

			splineTracker.interpolate(i);
			drawSplineLine(g, i);
		}
	}


	private void drawSplineLine(Graphics g, int time) {
		renderEndPoint.set(getEndX(time), getEndY());
		GU.drawLines(g, renderStartPoint, renderEndPoint);
		renderStartPoint.set(renderEndPoint);
	}

	private int getEndX(int time) {
		return Math.round(pixPerUnitX * time);
	}

	private int getEndY() {
		return getHeight() - Math.round(pixPerUnitY * splineTracker.getEndX());
	}
}
