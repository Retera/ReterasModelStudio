package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class TVertexScalerWidget {
	private static final int LINE_LEN = 80;
	private static final int EXTERIOR_TRIANGLE_OFFSET = LINE_LEN - 16;
	private static final int INTERIOR_TRIANGLE_OFFSET = LINE_LEN - 32;
	private final Vec2 point;
	private final int[] triangleXPoints = new int[3];
	private final int[] triangleYPoints = new int[3];
	private final int[] rombXPoints4 = new int[4];
	private final int[] rombYPoints4 = new int[4];
	private MoveDimension moveDirection = MoveDimension.NONE;

	public TVertexScalerWidget(final Vec2 point) {
		this.point = new Vec2(0, 0);
		this.point.set(point);
	}

	public MoveDimension getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem, final byte dim1, final byte dim2) {
		final double x = coordinateSystem.convertX(point.getCoord(dim1));
		final double y = coordinateSystem.convertY(point.getCoord(dim2));
		MoveDimension direction = MoveDimension.NONE;

		if (mousePoint.x > x && mousePoint.y < y && mousePoint.y - y > mousePoint.x - x - INTERIOR_TRIANGLE_OFFSET) {
			return MoveDimension.XYZ;
		}
		if ((Math.abs(x - mousePoint.getX()) <= 4 && mousePoint.y < y && mousePoint.y > y - LINE_LEN)) {
			return MoveDimension.getByByte(dim2);
		}
		if ((Math.abs(y - mousePoint.getY()) <= 4 && mousePoint.x > x && mousePoint.x < x + LINE_LEN)) {
			return MoveDimension.getByByte(dim1);
		}
		if (mousePoint.x > x && mousePoint.y < y && mousePoint.y - y > mousePoint.x - x - EXTERIOR_TRIANGLE_OFFSET && mousePoint.y - y < mousePoint.x - x - INTERIOR_TRIANGLE_OFFSET) {
			return MoveDimension.getByByte(dim1, dim2);
		}
		return direction;
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
		final double x = coordinateSystem.convertX(point.getCoord(xDimension));
		final double y = coordinateSystem.convertY(point.getCoord(yDimension));
		if (moveDirection != null) {
			setHighLightableColor(graphics, xDimension, moveDirection);
			drawEastLine(graphics, (int) x, (int) y);
			drawEastBox(graphics, (int) x, (int) y);
			drawDiagonalLineEast(graphics, (int) x, (int) y, EXTERIOR_TRIANGLE_OFFSET);
			drawDiagonalLineEast(graphics, (int) x, (int) y, INTERIOR_TRIANGLE_OFFSET);
			setHighLightableColor(graphics, yDimension, moveDirection);
			drawNorthLine(graphics, (int) x, (int) y);
			drawNorthBox(graphics, (int) x, (int) y);
			drawDiagonalLineNorth(graphics, (int) x, (int) y, EXTERIOR_TRIANGLE_OFFSET);
			drawDiagonalLineNorth(graphics, (int) x, (int) y, INTERIOR_TRIANGLE_OFFSET);
			switch (moveDirection) {
				case NONE -> {
					updateTriangle((int) x, (int) y);
					graphics.setColor(new Color(255, 255, 255, 70));
					graphics.fillPolygon(triangleXPoints, triangleYPoints, 3);
				}
				case XYZ -> {
					updateTriangle((int) x, (int) y);
					graphics.setColor(new Color(255, 255, 0, 70));
					graphics.fillPolygon(triangleXPoints, triangleYPoints, 3);
					graphics.setColor(new Color(255, 255, 0));
					graphics.drawPolygon(triangleXPoints, triangleYPoints, 3);
				}
				case XY, XZ, YZ -> {
					updateRomb((int) x, (int) y);
					graphics.setColor(new Color(255, 255, 255, 70));
					graphics.fillPolygon(rombXPoints4, rombYPoints4, 4);
					graphics.setColor(new Color(180, 255, 0));
					graphics.drawPolygon(rombXPoints4, rombYPoints4, 4);
				}
			}
		}
	}

	public void updateRomb(int x, int y) {
		rombXPoints4[0] = x + EXTERIOR_TRIANGLE_OFFSET;
		rombYPoints4[0] = y;
		rombXPoints4[1] = x + INTERIOR_TRIANGLE_OFFSET;
		rombYPoints4[1] = y;
		rombXPoints4[2] = x;
		rombYPoints4[2] = y - INTERIOR_TRIANGLE_OFFSET;
		rombXPoints4[3] = x;
		rombYPoints4[3] = y - EXTERIOR_TRIANGLE_OFFSET;
	}

	public void updateTriangle(int x, int y) {
		triangleXPoints[0] = x;
		triangleXPoints[0] = y;
		triangleXPoints[1] = x + INTERIOR_TRIANGLE_OFFSET;
		triangleXPoints[1] = y;
		triangleXPoints[2] = x;
		triangleXPoints[2] = y - INTERIOR_TRIANGLE_OFFSET;
	}

	public void drawDiagonalLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x + EXTERIOR_TRIANGLE_OFFSET, y, x, y - EXTERIOR_TRIANGLE_OFFSET);
	}

	public void drawDiagonalLineNorth(Graphics2D graphics, int x, int y, int triangleOffset) {
		graphics.drawLine(x + triangleOffset / 2, y - triangleOffset / 2, x, y - triangleOffset);
	}

	public void drawDiagonalLineEast(Graphics2D graphics, int x, int y, int triangleOffset) {
		graphics.drawLine(x + triangleOffset, y, x + triangleOffset / 2, y - triangleOffset / 2);
	}

	public void drawNorthBox(Graphics2D graphics, int x, int y) {
		graphics.drawRect(x - 2, y - LINE_LEN - 2, 4, 4);
	}

	public void drawEastBox(Graphics2D graphics, int x, int y) {
		graphics.drawRect(x + LINE_LEN - 2, y - 2, 4, 4);
	}

	public void drawNorthLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x, y, x, y - LINE_LEN);
	}

	public void drawEastLine(Graphics2D graphics, int x, int y) {
		graphics.drawLine(x, y, x + LINE_LEN, y);
	}

	private void setColorByDimension(final Graphics2D graphics, final byte dimension) {
		switch (dimension) {
			case 0, -1 -> graphics.setColor(new Color(0, 255, 0));
			case 1, -2 -> graphics.setColor(new Color(255, 0, 0));
			case 2, -3 -> graphics.setColor(new Color(0, 0, 255));
		}
	}

	private void setHighLightableColor(final Graphics2D graphics, final byte dimension, MoveDimension moveDimension) {
		if (moveDimension.containDirection(dimension)) {
			graphics.setColor(new Color(255, 255, 0));
		} else {
			setColorByDimension(graphics, dimension);
		}
	}
}
