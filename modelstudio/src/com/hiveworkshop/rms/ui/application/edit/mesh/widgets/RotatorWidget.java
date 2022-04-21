package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class RotatorWidget extends Widget {
	private static final int ROTATOR_RADIUS = 60;
	private static final int ROTATOR_RADIUS_SQUARED = ROTATOR_RADIUS * ROTATOR_RADIUS;

	public RotatorWidget() {
	}

	@Override
	public MoveDimension getDirectionByMouse(Vec2 mousePoint, CoordinateSystem coordinateSystem) {
		if(coordinateSystem != null){
			byte dim1 = coordinateSystem.getPortFirstXYZ();
			byte dim2 = coordinateSystem.getPortSecondXYZ();
			double x = coordinateSystem.viewX(point.getCoord(dim1));
			double y = coordinateSystem.viewY(point.getCoord(dim2));

			double deltaX = x - mousePoint.x;
			double deltaY = y - mousePoint.y;
			if (Math.abs(deltaX) <= 3 && Math.abs(deltaY) <= ROTATOR_RADIUS) {
				return MoveDimension.getByByte(dim1);
			}
			if (Math.abs(deltaX) <= ROTATOR_RADIUS && Math.abs(deltaY) <= 3) {
				return MoveDimension.getByByte(dim2);
			}
			double dstSquared = deltaY * deltaY + deltaX * deltaX;
			if (Math.abs(Math.sqrt(dstSquared) - ROTATOR_RADIUS) <= 3) {
				return MoveDimension.getByByte(getOutwardDimension(dim1, dim2));
			}
			if (dstSquared < ROTATOR_RADIUS_SQUARED) {
				return MoveDimension.NONE;
			}
		}

		return MoveDimension.NONE;
	}

	public Vec3 getPoint() {
		return point;
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		byte xDimension = coordinateSystem.getPortFirstXYZ();
		byte yDimension = coordinateSystem.getPortSecondXYZ();
		double x = coordinateSystem.viewX(point.getCoord(xDimension));
		double y = coordinateSystem.viewY(point.getCoord(yDimension));

		setHighLightableColor(graphics, yDimension, moveDirection);
		drawHorzLine(graphics, x, y);
		setHighLightableColor(graphics, xDimension, moveDirection);
		drawVertLine(graphics, x, y);
		setHighLightableColor(graphics, getOutwardDimension(xDimension, yDimension), moveDirection);
//        setColorByDimension(graphics, getOutwardDimension(xDimension, yDimension));
		drawCircle(graphics, x, y);

		if (moveDirection != null) {
		    switch (moveDirection) {
			    case XY -> {
				    graphics.setColor(new Color(0.5f, 0.5f, 0.5f, 0.4f));
				    graphics.fillOval((int) (x - ROTATOR_RADIUS), (int) (y - ROTATOR_RADIUS), ROTATOR_RADIUS * 2, ROTATOR_RADIUS * 2);
				    setColorByDimension(graphics, xDimension);
				    drawHorzLine(graphics, x, y);
				    setColorByDimension(graphics, yDimension);
				    drawVertLine(graphics, x, y);
				    setColorByDimension(graphics, getOutwardDimension(xDimension, yDimension));
				    drawCircle(graphics, x, y);
			    }
		    }
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

	private byte getOutwardDimension(byte xDimension, byte yDimension) {
		return getUnusedXYZ(xDimension, yDimension);
	}

	public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}
}
