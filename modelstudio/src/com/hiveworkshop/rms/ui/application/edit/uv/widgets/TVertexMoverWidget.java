package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vector2;

public final class TVertexMoverWidget {
	private static final int TRIANGLE_OFFSET = 60 - 16;
	private final Vector2 point;
	private MoveDirection moveDirection = MoveDirection.NONE;
	private final Polygon northTriangle;
	private final Polygon eastTriangle;

	public TVertexMoverWidget(final Vector2 point) {
		this.point = new Vector2(0, 0);
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
		if (northTriangle.contains(mousePoint) || ((Math.abs(x - mousePoint.getX()) <= 1) && (mousePoint.y < y)
				&& (mousePoint.y > (y - TRIANGLE_OFFSET)))) {
			direction = MoveDirection.UP;
		}
		if (eastTriangle.contains(mousePoint) || ((Math.abs(y - mousePoint.getY()) <= 1) && (mousePoint.x > x)
				&& (mousePoint.x < (x + TRIANGLE_OFFSET)))) {
			direction = MoveDirection.RIGHT;
		}
		if (new Rectangle((int) x, (int) y - 20, 20, 20).contains(mousePoint)) {
			direction = MoveDirection.BOTH;
		}
		eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
		northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
		return direction;
	}

	public Vector2 getPoint() {
		return point;
	}

	public void setPoint(final Vector2 point) {
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
			case BOTH:
				graphics.setColor(new Color(255, 255, 0, 70));
				graphics.fillRect((int) x, (int) y - 20, 20, 20);
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawLine((int) x, (int) y - 15, (int) x, (int) y - 60);
				graphics.drawLine((int) x + 15, (int) y, (int) x + 60, (int) y);
				graphics.drawLine((int) x + 20, (int) y, (int) x + 20, (int) y - 20);
				graphics.drawLine((int) x, (int) y - 20, (int) x + 20, (int) y - 20);
				setColorByDimension(graphics, xDimension);
				eastTriangle.translate((int) x + TRIANGLE_OFFSET, (int) y);
				graphics.fill(eastTriangle);
				eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
				setColorByDimension(graphics, yDimension);
				northTriangle.translate((int) x, (int) y - TRIANGLE_OFFSET);
				graphics.fill(northTriangle);
				northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
				break;
			case UP:
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawLine((int) x, (int) y - 15, (int) x, (int) y - 60);
				setColorByDimension(graphics, xDimension);
				eastTriangle.translate((int) x + TRIANGLE_OFFSET, (int) y);
				graphics.drawLine((int) x + 15, (int) y, (int) x + 60, (int) y);
				graphics.drawLine((int) x + 20, (int) y, (int) x + 20, (int) y - 20);
				graphics.fill(eastTriangle);
				eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
				setColorByDimension(graphics, yDimension);
				northTriangle.translate((int) x, (int) y - TRIANGLE_OFFSET);
				graphics.drawLine((int) x, (int) y - 20, (int) x + 20, (int) y - 20);
				graphics.fill(northTriangle);
				northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
				break;
			case RIGHT:
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawLine((int) x + 15, (int) y, (int) x + 60, (int) y);
				setColorByDimension(graphics, xDimension);
				eastTriangle.translate((int) x + TRIANGLE_OFFSET, (int) y);
				graphics.drawLine((int) x + 20, (int) y, (int) x + 20, (int) y - 20);
				graphics.fill(eastTriangle);
				eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
				setColorByDimension(graphics, yDimension);
				northTriangle.translate((int) x, (int) y - TRIANGLE_OFFSET);
				graphics.drawLine((int) x, (int) y - 15, (int) x, (int) y - 60);
				graphics.drawLine((int) x, (int) y - 20, (int) x + 20, (int) y - 20);
				graphics.fill(northTriangle);
				northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
				break;
			case NONE:
				setColorByDimension(graphics, xDimension);
				eastTriangle.translate((int) x + TRIANGLE_OFFSET, (int) y);
				graphics.drawLine((int) x + 15, (int) y, (int) x + 60, (int) y);
				graphics.drawLine((int) x + 20, (int) y, (int) x + 20, (int) y - 20);
				graphics.fill(eastTriangle);
				eastTriangle.translate(-((int) x + TRIANGLE_OFFSET), -((int) y));
				setColorByDimension(graphics, yDimension);
				northTriangle.translate((int) x, (int) y - TRIANGLE_OFFSET);
				graphics.drawLine((int) x, (int) y - 15, (int) x, (int) y - 60);
				graphics.drawLine((int) x, (int) y - 20, (int) x + 20, (int) y - 20);
				graphics.fill(northTriangle);
				northTriangle.translate(-(int) x, -((int) y - TRIANGLE_OFFSET));
				break;
			}
		}
	}

	private void setColorByDimension(final Graphics2D graphics, final byte xDimension) {
		switch (xDimension) {
		case 0:
			graphics.setColor(new Color(0, 255, 0));
			break;
		case 1:
			graphics.setColor(new Color(255, 0, 0));
			break;
		case 2:
			graphics.setColor(new Color(0, 0, 255));
			break;
		}
	}

	public enum MoveDirection {
		UP, RIGHT, BOTH, NONE
	}
}
