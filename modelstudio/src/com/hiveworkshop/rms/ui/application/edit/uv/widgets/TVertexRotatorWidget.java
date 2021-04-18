package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class TVertexRotatorWidget {
	private static final int ROTATOR_RADIUS = 60;
	private static final int ROTATOR_RADIUS_SQUARED = ROTATOR_RADIUS * ROTATOR_RADIUS;
	private final Vec2 point;
	private MoveDimension moveDirection = MoveDimension.NONE;

	public TVertexRotatorWidget(final Vec2 point) {
		this.point = new Vec2(0, 0);
		this.point.set(point);
	}

	public MoveDimension getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem) {
		final double x = coordinateSystem.viewX(point.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.viewY(point.getCoord(coordinateSystem.getPortSecondXYZ()));

		final double deltaY = y - mousePoint.getY();
		final double deltaX = x - mousePoint.getX();
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		if (Math.abs(deltaX) <= 3 && Math.abs(deltaY) <= ROTATOR_RADIUS) {
			return MoveDimension.getByByte(dim1);
		}
		if (Math.abs(deltaX) <= ROTATOR_RADIUS && Math.abs(deltaY) <= 3) {
			return MoveDimension.getByByte(dim2);
		}
		final double dstSquared = deltaY * deltaY + deltaX * deltaX;
		if (Math.abs(Math.sqrt(dstSquared) - ROTATOR_RADIUS) <= 3) {
			return MoveDimension.getByByte(getOutwardDimension(dim1, dim2));
		}
		if (dstSquared < ROTATOR_RADIUS_SQUARED) {
			return MoveDimension.NONE;
		}

		return MoveDimension.NONE;
	}

	public Vec2 getPoint() {
		return point;
	}

	public void setPoint(final Vec2 point) {
		this.point.set(point);
	}

	public MoveDimension getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(final MoveDimension moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		final byte xDimension = coordinateSystem.getPortFirstXYZ();
		final byte yDimension = coordinateSystem.getPortSecondXYZ();
		final double x = coordinateSystem.viewX(point.getCoord(xDimension));
		final double y = coordinateSystem.viewY(point.getCoord(yDimension));

		setHighLightableColor(graphics, yDimension, moveDirection);
		drawHorzLine(graphics, x, y);
		setHighLightableColor(graphics, xDimension, moveDirection);
		drawVertLine(graphics, x, y);
		setHighLightableColor(graphics, getOutwardDimension(xDimension, yDimension), moveDirection);
		drawCircle(graphics, x, y);


		if (moveDirection == MoveDimension.XY) {
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


	public void drawCircle(Graphics2D graphics, double x, double y) {
		graphics.drawOval((int) (x - ROTATOR_RADIUS), (int) (y - ROTATOR_RADIUS), ROTATOR_RADIUS * 2, ROTATOR_RADIUS * 2);
	}

	public void drawVertLine(Graphics2D graphics, double x, double y) {
		graphics.drawLine((int) (x), (int) (y - ROTATOR_RADIUS), (int) (x), (int) (y + ROTATOR_RADIUS));
	}

	public void drawHorzLine(Graphics2D graphics, double x, double y) {
		graphics.drawLine((int) (x - ROTATOR_RADIUS), (int) y, (int) (x + ROTATOR_RADIUS), (int) y);
	}

	private byte getOutwardDimension(final byte xDimension, final byte yDimension) {
		return CoordinateSystem.Util.getUnusedXYZ(xDimension, yDimension);
	}

//	private byte getOutwardDimension(final byte xDimension, final byte yDimension) {
//		return (byte) (3 - xDimension - yDimension);
//	}

	private void setColorByDimension(final Graphics2D graphics, final byte dimension) {
		switch (dimension) {
			case 0, -1 -> graphics.setColor(new Color(0, 255, 0));
			case 1, -2 -> graphics.setColor(new Color(255, 0, 0));
			case 2, -3 -> graphics.setColor(new Color(0, 0, 255));
		}
	}

	private void setHighLightableColor(final Graphics2D graphics, final byte dimension, MoveDimension moveDimension) {
//		System.out.println(moveDimension + " has " + MoveDimension.getByByte(dimension) + "?");
		if (moveDimension.containDirection(dimension)) {
			graphics.setColor(new Color(255, 255, 0));
		} else {
			setColorByDimension(graphics, dimension);
		}
	}
}
