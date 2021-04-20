package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class RotatorWidget {
	private static final int ROTATOR_RADIUS = 60;
	private static final int ROTATOR_RADIUS_SQUARED = ROTATOR_RADIUS * ROTATOR_RADIUS;
	private final Vec3 point;
	private MoveDimension moveDirection = MoveDimension.NONE;

	public RotatorWidget(Vec3 point) {
		this.point = new Vec3(0, 0, 0);
		this.point.set(point);
	}

	public MoveDimension getDirectionByMouse(Point mousePoint, CoordinateSystem coordinateSystem) {
		double x = coordinateSystem.viewX(point.getCoord(coordinateSystem.getPortFirstXYZ()));
		double y = coordinateSystem.viewY(point.getCoord(coordinateSystem.getPortSecondXYZ()));

		double deltaY = y - mousePoint.getY();
		double deltaX = x - mousePoint.getX();
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
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

		return MoveDimension.NONE;
	}

    public Vec3 getPoint() {
        return point;
    }

	public void setPoint(Vec3 point) {
		this.point.set(point);
	}

	public MoveDimension getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(MoveDimension moveDirection) {
		this.moveDirection = moveDirection;
	}

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
		return CoordinateSystem.Util.getUnusedXYZ(xDimension, yDimension);
	}

	private void setColorByDimension(Graphics2D graphics, byte dimension) {
		switch (dimension) {
			case 0, -1 -> graphics.setColor(new Color(0, 255, 0));
			case 1, -2 -> graphics.setColor(new Color(255, 0, 0));
			case 2, -3 -> graphics.setColor(new Color(0, 0, 255));
		}
	}

	private void setHighLightableColor(Graphics2D graphics, byte dimension, MoveDimension moveDimension) {
//		System.out.println(moveDimension + " has " + MoveDimension.getByByte(dimension) + "?");
		if (moveDimension.containDirection(dimension)) {
			graphics.setColor(new Color(255, 255, 0));
		} else {
			setColorByDimension(graphics, dimension);
		}
	}
}
