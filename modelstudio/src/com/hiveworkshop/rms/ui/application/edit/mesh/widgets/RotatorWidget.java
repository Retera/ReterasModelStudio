package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class RotatorWidget extends Widget {
	private static final int ROTATOR_RADIUS = 60;
	private static final int ROTATOR_RADIUS_SQUARED = ROTATOR_RADIUS * ROTATOR_RADIUS;

	public RotatorWidget() {
	}


	public MoveDimension getDirectionByMouse(Vec2 mousePoint1, Mat4 viewportMat, Component parent) {
		Vec2 vpPoint = getVpPoint(viewportMat, parent);
		int x = (int) vpPoint.x;
		int y = (int) vpPoint.y;

		double deltaX = x - ((1 + mousePoint1.x)/2f*parent.getWidth());
		double deltaY = y - ((1 - mousePoint1.y)/2f*parent.getHeight());
		if (Math.abs(deltaX) <= 3 && Math.abs(deltaY) <= ROTATOR_RADIUS) {
			return MoveDimension.X;
		}
		if (Math.abs(deltaX) <= ROTATOR_RADIUS && Math.abs(deltaY) <= 3) {
			return MoveDimension.Y;
		}
		double dstSquared = deltaY * deltaY + deltaX * deltaX;
		if (Math.abs(Math.sqrt(dstSquared) - ROTATOR_RADIUS) <= 3) {
			return MoveDimension.Z;
		}
		if (dstSquared < ROTATOR_RADIUS_SQUARED) {
			return MoveDimension.NONE;
		}

		return MoveDimension.NONE;
	}

	public Vec3 getPoint() {
		return point;
	}

	public void render(Graphics2D graphics, Mat4 viewportMat, Mat4 invViewportMat, Component parent){
		float aspect = parent.getWidth() / (float) parent.getHeight();
		temp0.set(0, 0, 0).transform(invViewportMat, 1, true);

		Vec3 tempX = tempPoint.set(1, 0, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension xDim = MoveDimension.getByAxis(tempX.normalize());

		Vec3 tempY = tempPoint.set(0, aspect*1, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension yDim = MoveDimension.getByAxis(tempY.normalize());

		MoveDimension zDim = MoveDimension.values()[MoveDimension.XYZ.ordinal() - xDim.ordinal() - yDim.ordinal()];

		Vec3 tempTot = tempPoint.set(1, aspect*1, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension totDim = MoveDimension.getByAxis(tempTot.normalize());

		Vec2 vpPoint = getVpPoint(viewportMat, parent);
		int x = (int) vpPoint.x;
		int y = (int) vpPoint.y;

		graphics.setColor(getHighLightableColor(yDim, moveDirection == yDim || moveDirection == totDim));
		drawHorzLine(graphics, x, y);
		graphics.setColor(getHighLightableColor(xDim, moveDirection == xDim || moveDirection == totDim));
		drawVertLine(graphics, x, y);
		graphics.setColor(getHighLightableColor(zDim, moveDirection == zDim || moveDirection == totDim));
		drawCircle(graphics, x, y);

		if (moveDirection != null && moveDirection == MoveDimension.XY) {
			graphics.setColor(new Color(0.5f, 0.5f, 0.5f, 0.4f));
			graphics.fillOval(x - ROTATOR_RADIUS, y - ROTATOR_RADIUS, ROTATOR_RADIUS * 2, ROTATOR_RADIUS * 2);
			graphics.setColor(getColor(xDim));
			drawHorzLine(graphics, x, y);
			graphics.setColor(getColor(yDim));
			drawVertLine(graphics, x, y);
			graphics.setColor(getColor(zDim));
			drawCircle(graphics, x, y);
		}
	}

	public void drawCircle(Graphics2D graphics, double x, double y) {
		graphics.drawOval((int) (x - ROTATOR_RADIUS), (int) (y - ROTATOR_RADIUS), ROTATOR_RADIUS * 2, ROTATOR_RADIUS * 2);
	}

	public void drawVertLine(Graphics2D graphics, double x, double y) {
		graphics.drawLine((int) (x), (int) (y - ROTATOR_RADIUS), (int) (x), (int) (y + ROTATOR_RADIUS));
	}

	public void drawHorzLine(Graphics2D graphics, double x, double y) {
		graphics.drawLine((int) (x - ROTATOR_RADIUS), (int) y, (int) (x + ROTATOR_RADIUS), (int) y);
	}
}
