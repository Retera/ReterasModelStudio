package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class MoverWidget {
	private static final int TRIANGLE_OFFSET = 60 - 16;
	private final Vec3 point;
	private MoveDirection moveDirection = MoveDirection.NONE;
	private final Polygon northTriangle;
	private final Polygon eastTriangle;

	public MoverWidget(final Vec3 point) {
		this.point = new Vec3(0, 0, 0);
		this.point.set(point);
		northTriangle = new Polygon();
		northTriangle.addPoint(-5, 0);
		northTriangle.addPoint(0, -18);
		northTriangle.addPoint(5, 0);

		eastTriangle = new Polygon();
		eastTriangle.addPoint(0, -5);
		eastTriangle.addPoint(18, 0);
		eastTriangle.addPoint(0, 5);
	}

	public MoveDirection getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem,
											 final byte dim1, final byte dim2) {
		final double x = coordinateSystem.convertX(point.getCoord(dim1));
		final double y = coordinateSystem.convertY(point.getCoord(dim2));
		eastTriangle.translate((int) x + TRIANGLE_OFFSET, (int) y);
		northTriangle.translate((int) x, (int) y - TRIANGLE_OFFSET);
		MoveDirection direction = MoveDirection.NONE;
		if (northTriangle.contains(mousePoint)
				|| (Math.abs(x - mousePoint.getX()) <= 1
				&& mousePoint.y < y
				&& mousePoint.y > y - TRIANGLE_OFFSET)) {
			direction = MoveDirection.UP;
		}
		if (eastTriangle.contains(mousePoint)
				|| (Math.abs(y - mousePoint.getY()) <= 1
				&& mousePoint.x > x
				&& mousePoint.x < x + TRIANGLE_OFFSET)) {
			direction = MoveDirection.RIGHT;
		}
		if (new Rectangle((int) x, (int) y - 20, 20, 20).contains(mousePoint)) {
			direction = MoveDirection.BOTH;
		}
		eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
		northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
		return direction;
	}

	public Vec3 getPoint() {
		return point;
	}

	public void setPoint(final Vec3 point) {
		this.point.set(point);
	}

	public MoveDirection getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(final MoveDirection moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		final byte xDimension = coordinateSystem.getPortFirstXYZ();
		final byte yDimension = coordinateSystem.getPortSecondXYZ();
		final double x = coordinateSystem.convertX(point.getCoord(xDimension));
		final double y = coordinateSystem.convertY(point.getCoord(yDimension));
		if (moveDirection != null) {
			switch (moveDirection) {
				case BOTH -> {
					graphics.setColor(new Color(255, 255, 0, 70));
					graphics.fillRect((int) x, (int) y - 20, 20, 20);
					graphics.setColor(new Color(255, 255, 0));
					drawLongNorthLine(graphics, (int) x, (int) y);
					drawLongEatsLine(graphics, (int) x, (int) y);
					drawShortNorthLine(graphics, (int) x, (int) y);
					drawShortEastLine(graphics, (int) x, (int) y);
					setColorByDimension(graphics, xDimension);
					drawEastTriangle(graphics, (int) x, (int) y);
					setColorByDimension(graphics, yDimension);
					drawNorthTriangle(graphics, (int) x, (int) y);
				}
				case UP -> {
					graphics.setColor(new Color(255, 255, 0));
					drawLongNorthLine(graphics, (int) x, (int) y);
					setColorByDimension(graphics, xDimension);
					drawLongEatsLine(graphics, (int) x, (int) y);
					drawShortNorthLine(graphics, (int) x, (int) y);
					drawEastTriangle(graphics, (int) x, (int) y);
					setColorByDimension(graphics, yDimension);
					drawShortEastLine(graphics, (int) x, (int) y);
					drawNorthTriangle(graphics, (int) x, (int) y);
				}
				case RIGHT -> {
					graphics.setColor(new Color(255, 255, 0));
					drawLongEatsLine(graphics, (int) x, (int) y);
					setColorByDimension(graphics, xDimension);
					drawShortNorthLine(graphics, (int) x, (int) y);
					drawEastTriangle(graphics, (int) x, (int) y);
					setColorByDimension(graphics, yDimension);
					drawLongNorthLine(graphics, (int) x, (int) y);
					drawShortEastLine(graphics, (int) x, (int) y);
					drawNorthTriangle(graphics, (int) x, (int) y);
				}
				case NONE -> {
					setColorByDimension(graphics, xDimension);
					drawLongEatsLine(graphics, (int) x, (int) y);
					drawShortNorthLine(graphics, (int) x, (int) y);
					drawEastTriangle(graphics, (int) x, (int) y);
					setColorByDimension(graphics, yDimension);
					drawLongNorthLine(graphics, (int) x, (int) y);
					drawShortEastLine(graphics, (int) x, (int) y);
					drawNorthTriangle(graphics, (int) x, (int) y);
				}
			}
		}
	}

	private void drawShortEastLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x, y - 20, x + 20, y - 20);
	}

	private void drawShortNorthLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x + 20, y, x + 20, y - 20);
	}

	private void drawLongEatsLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x + 15, y, x + 60, y);
	}

	private void drawLongNorthLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x, y - 15, x, y - 60);
	}

	private void drawEastTriangle(Graphics2D graphics, int x, int y) {
		eastTriangle.translate(x + TRIANGLE_OFFSET, y);
		graphics.fill(eastTriangle);
		eastTriangle.translate(-(x + TRIANGLE_OFFSET), -y);
	}

	private void drawNorthTriangle(Graphics2D graphics, int x, int y) {
		northTriangle.translate(x, y - TRIANGLE_OFFSET);
		graphics.fill(northTriangle);
		northTriangle.translate(-x, -(y - TRIANGLE_OFFSET));
	}

	private void setColorByDimension(final Graphics2D graphics, final byte dimension) {
		switch (dimension) {
			case 0, -1 -> graphics.setColor(new Color(0, 255, 0));
			case 1, -2 -> graphics.setColor(new Color(255, 0, 0));
			case 2, -3 -> graphics.setColor(new Color(0, 0, 255));
		}
	}

	public enum MoveDirection {
		UP, RIGHT, BOTH, NONE
	}
}
