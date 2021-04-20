package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class TVertexMoverWidget {
	private static final int LINE_LONG = 60;
	private static final int LINE_SHORT = 20;
	private static final int TRIANGLE_OFFSET = LINE_LONG - 16;
	private final Vec2 point;
	private MoveDimension moveDirection = MoveDimension.NONE;
	private final Polygon northTriangle;
	private final Polygon eastTriangle;

	private final Polygon eastLineHitBox;
	private final Polygon northLineHitBox;

	public TVertexMoverWidget(final Vec2 point) {
		this.point = new Vec2(0, 0);
		this.point.set(point);

		northTriangle = GU.getSymTriPoly(5, 0, -18);
		northTriangle.translate(0, -TRIANGLE_OFFSET);

		eastTriangle = GU.getSymTriPoly(0, 5, 18);
		eastTriangle.translate(TRIANGLE_OFFSET, 0);

		eastLineHitBox = GU.getRektPoly(0, -1, LINE_LONG, 1);

		northLineHitBox = GU.getRektPoly(-1, 0, 1, -LINE_LONG);
	}

	public MoveDimension getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem, final byte dim1, final byte dim2) {
		final double x = coordinateSystem.viewX(point.getCoord(dim1));
		final double y = coordinateSystem.viewY(point.getCoord(dim2));

		MoveDimension direction = MoveDimension.NONE;

		if (GU.getTransPolygon((int) x, (int) y, northTriangle).contains(mousePoint)
				|| GU.getTransPolygon((int) x, (int) y, northLineHitBox).contains(mousePoint)) {
			direction = MoveDimension.getByByte(dim2);
		}
		if (GU.getTransPolygon((int) x, (int) y, eastTriangle).contains(mousePoint)
				|| GU.getTransPolygon((int) x, (int) y, eastLineHitBox).contains(mousePoint)) {
			direction = MoveDimension.getByByte(dim1);
		}
		if (new Rectangle((int) x, (int) y - LINE_SHORT, LINE_SHORT, LINE_SHORT).contains(mousePoint)) {
			direction = MoveDimension.getByByte(dim1, dim2);
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

		setHighLightableColor(graphics, yDimension, moveDirection);
		drawNorthArrow(graphics, x, y);

		setHighLightableColor(graphics, xDimension, moveDirection);
		drawEastArrow(graphics, x, y);

		setColorByDimension(graphics, xDimension);
		drawNorthLine(graphics, x, y, LINE_SHORT, LINE_SHORT);

		setColorByDimension(graphics, yDimension);
		drawEastLine(graphics, x, y, LINE_SHORT, LINE_SHORT);

		if (moveDirection.containDirection(xDimension) && moveDirection.containDirection(yDimension)) {
			graphics.setColor(new Color(255, 255, 0, 70));
			graphics.fillRect(x, y - LINE_SHORT, LINE_SHORT, LINE_SHORT);
		}
	}

	public void drawEastArrow(Graphics2D graphics, int x, int y) {
		drawEastLine(graphics, x, y, LINE_LONG, 0);
		GU.fillPolygonAt(graphics, x, y, eastTriangle);
	}

	public void drawNorthArrow(Graphics2D graphics, int x, int y) {
		drawNorthLine(graphics, x, y, LINE_LONG, 0);
		GU.fillPolygonAt(graphics, x, y, northTriangle);
	}

	private void drawEastLine(Graphics2D graphics, int x, int y, int length, int offset) {
		graphics.drawLine(x, y - offset, x + length, y - offset);
	}

	private void drawNorthLine(Graphics2D graphics, int x, int y, int length, int offset) {
		graphics.drawLine(x + offset, y, x + offset, y - length);
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
