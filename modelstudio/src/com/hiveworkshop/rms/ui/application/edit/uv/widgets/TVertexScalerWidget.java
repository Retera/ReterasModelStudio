package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class TVertexScalerWidget {
	private static final int LINE_LEN = 80;
	private static final int LINE_SENS = 3;
	private static final int EXTERIOR_TRIANGLE_OFFSET = LINE_LEN - 16;
	private static final int INTERIOR_TRIANGLE_OFFSET = LINE_LEN - 32;
	private final Vec2 point;
	private final Polygon romb;
	private final Polygon triangle;
	private MoveDimension moveDirection = MoveDimension.NONE;

	private final Polygon northLineHitBox;
	private final Polygon eastLineHitBox;

	public TVertexScalerWidget(final Vec2 point) {
		this.point = new Vec2(0, 0);
		this.point.set(point);

		triangle = new Polygon();
		triangle.addPoint(0, 0);
		triangle.addPoint(INTERIOR_TRIANGLE_OFFSET, 0);
		triangle.addPoint(0, -INTERIOR_TRIANGLE_OFFSET);

		romb = new Polygon();
		romb.addPoint(EXTERIOR_TRIANGLE_OFFSET, 0);
		romb.addPoint(INTERIOR_TRIANGLE_OFFSET, 0);
		romb.addPoint(0, -INTERIOR_TRIANGLE_OFFSET);
		romb.addPoint(0, -EXTERIOR_TRIANGLE_OFFSET);

		northLineHitBox = GU.getRektPoly(-LINE_SENS, 0, LINE_SENS, -LINE_LEN - LINE_SENS);
		eastLineHitBox = GU.getRektPoly(0, -LINE_SENS, LINE_LEN + LINE_SENS, LINE_SENS);
	}

	public MoveDimension getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem, final byte dim1, final byte dim2) {
		final int x = (int) coordinateSystem.viewX(point.getCoord(dim1));
		final int y = (int) coordinateSystem.viewY(point.getCoord(dim2));

		MoveDimension direction = MoveDimension.NONE;

		if (GU.getTransPolygon(x, y, triangle).contains(mousePoint)) {
			return MoveDimension.XYZ;
		}
		if (GU.getTransPolygon(x, y, northLineHitBox).contains(mousePoint)) {
			return MoveDimension.getByByte(dim2);
		}
		if (GU.getTransPolygon(x, y, eastLineHitBox).contains(mousePoint)) {
			return MoveDimension.getByByte(dim1);
		}
		if (GU.getTransPolygon(x, y, romb).contains(mousePoint)) {
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
		final int x = (int) coordinateSystem.viewX(point.getCoord(xDimension));
		final int y = (int) coordinateSystem.viewY(point.getCoord(yDimension));
		if (moveDirection != null) {
			setHighLightableColor(graphics, xDimension, moveDirection);
			drawEastLine(graphics, x, y);
			drawDiagonalLineEast(graphics, x, y, EXTERIOR_TRIANGLE_OFFSET);
			drawDiagonalLineEast(graphics, x, y, INTERIOR_TRIANGLE_OFFSET);

			setHighLightableColor(graphics, yDimension, moveDirection);
			drawNorthLine(graphics, x, y);
			drawDiagonalLineNorth(graphics, x, y, EXTERIOR_TRIANGLE_OFFSET);
			drawDiagonalLineNorth(graphics, x, y, INTERIOR_TRIANGLE_OFFSET);
			switch (moveDirection) {
				case NONE -> {
					graphics.setColor(new Color(255, 255, 255, 70));
					GU.fillPolygonAt(graphics, x, y, triangle);
				}
				case XYZ -> {
					graphics.setColor(new Color(255, 255, 0, 70));
					GU.fillPolygonAt(graphics, x, y, triangle);
					graphics.setColor(new Color(255, 255, 0));
					GU.drawPolygonAt(graphics, x, y, triangle);
				}
				case XY, XZ, YZ -> {
					graphics.setColor(new Color(255, 255, 255, 70));
					GU.fillPolygonAt(graphics, x, y, romb);
					graphics.setColor(new Color(180, 255, 0));
					GU.drawPolygonAt(graphics, x, y, romb);
				}
			}
		}
	}

	public void drawDiagonalLineNorth(Graphics2D graphics, int x, int y, int triangleOffset) {
		graphics.drawLine(x + triangleOffset / 2, y - triangleOffset / 2, x, y - triangleOffset);
	}

	public void drawDiagonalLineEast(Graphics2D graphics, int x, int y, int triangleOffset) {
		graphics.drawLine(x + triangleOffset, y, x + triangleOffset / 2, y - triangleOffset / 2);
	}

	public void drawNorthLine(Graphics2D graphics, int x, int y) {
		GU.fillCenteredSquare(graphics, x, y - LINE_LEN, 4);
		graphics.drawLine(x, y, x, y - LINE_LEN);
	}

	public void drawEastLine(Graphics2D graphics, int x, int y) {
		GU.fillCenteredSquare(graphics, x + LINE_LEN, y, 4);
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
