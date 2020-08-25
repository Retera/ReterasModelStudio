package com.hiveworkshop.rms.ui.application.edit.uv.widgets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vertex2;

public final class TVertexScalerWidget {
	private static final int LINE_LEN = 80;
	private static final int EXTERIOR_TRIANGLE_OFFSET = LINE_LEN - 16;
	private static final int INTERIOR_TRIANGLE_OFFSET = LINE_LEN - 32;
	private final Vertex2 point;
	private ScaleDirection moveDirection = ScaleDirection.NONE;
	private final int[] recycleXPoints = new int[3];
	private final int[] recycleYPoints = new int[3];
	private final int[] recycleXPoints4 = new int[4];
	private final int[] recycleYPoints4 = new int[4];

	public TVertexScalerWidget(final Vertex2 point) {
		this.point = new Vertex2(0, 0);
		this.point.setTo(point);
	}

	public ScaleDirection getDirectionByMouse(final Point mousePoint, final CoordinateSystem coordinateSystem,
			final byte dim1, final byte dim2) {
		final double x = coordinateSystem.convertX(point.getCoord(dim1));
		final double y = coordinateSystem.convertY(point.getCoord(dim2));
		ScaleDirection direction = ScaleDirection.NONE;
		if ((mousePoint.x > x) && (mousePoint.y < y)
				&& ((mousePoint.y - y) > (mousePoint.x - x - INTERIOR_TRIANGLE_OFFSET))) {
			return ScaleDirection.XYZ;
		}
		if (((Math.abs(x - mousePoint.getX()) <= 4) && (mousePoint.y < y) && (mousePoint.y > (y - LINE_LEN)))) {
			direction = ScaleDirection.UP;
		}
		if (((Math.abs(y - mousePoint.getY()) <= 4) && (mousePoint.x > x) && (mousePoint.x < (x + LINE_LEN)))) {
			direction = ScaleDirection.RIGHT;
		}
		if ((mousePoint.x > x) && (mousePoint.y < y)
				&& ((mousePoint.y - y) > (mousePoint.x - x - EXTERIOR_TRIANGLE_OFFSET))
				&& ((mousePoint.y - y) < (mousePoint.x - x - INTERIOR_TRIANGLE_OFFSET))) {
			return ScaleDirection.FLAT_XY;
		}
		return direction;
	}

	public Vertex2 getPoint() {
		return point;
	}

	public void setPoint(final Vertex2 point) {
		this.point.setTo(point);
	}

	public ScaleDirection getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(final ScaleDirection moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		final byte xDimension = coordinateSystem.getPortFirstXYZ();
		final byte yDimension = coordinateSystem.getPortSecondXYZ();
		final double x = coordinateSystem.convertX(point.getCoord(xDimension));
		final double y = coordinateSystem.convertY(point.getCoord(yDimension));
		if (moveDirection != null) {
			switch (moveDirection) {
			case XYZ:
				recycleXPoints[0] = (int) x;
				recycleYPoints[0] = (int) y;
				recycleXPoints[1] = (int) x + INTERIOR_TRIANGLE_OFFSET;
				recycleYPoints[1] = (int) y;
				recycleXPoints[2] = (int) x;
				recycleYPoints[2] = (int) y - INTERIOR_TRIANGLE_OFFSET;
				graphics.setColor(new Color(255, 255, 0, 70));
				graphics.fillPolygon(recycleXPoints, recycleYPoints, 3);
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawPolygon(recycleXPoints, recycleYPoints, 3);
				graphics.drawLine((int) x + EXTERIOR_TRIANGLE_OFFSET, (int) y, (int) x,
						(int) y - EXTERIOR_TRIANGLE_OFFSET);
				setColorByDimension(graphics, xDimension);
				graphics.drawLine((int) x, (int) y, (int) x + LINE_LEN, (int) y);
				graphics.drawRect(((int) x + LINE_LEN) - 2, (int) y - 2, 4, 4);
				setColorByDimension(graphics, yDimension);
				graphics.drawLine((int) x, (int) y, (int) x, (int) y - LINE_LEN);
				graphics.drawRect((int) x - 2, (int) y - LINE_LEN - 2, 4, 4);
				break;
			case UP:
				setColorByDimension(graphics, xDimension);
				graphics.drawLine((int) x + EXTERIOR_TRIANGLE_OFFSET, (int) y, (int) x + (EXTERIOR_TRIANGLE_OFFSET / 2),
						(int) y - (EXTERIOR_TRIANGLE_OFFSET / 2));
				graphics.drawLine((int) x + INTERIOR_TRIANGLE_OFFSET, (int) y, (int) x + (INTERIOR_TRIANGLE_OFFSET / 2),
						(int) y - (INTERIOR_TRIANGLE_OFFSET / 2));
				graphics.drawLine((int) x, (int) y, (int) x + LINE_LEN, (int) y);
				graphics.drawRect(((int) x + LINE_LEN) - 2, (int) y - 2, 4, 4);
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawLine((int) x, (int) y, (int) x, (int) y - LINE_LEN);
				setColorByDimension(graphics, yDimension);
				graphics.drawLine((int) x + (EXTERIOR_TRIANGLE_OFFSET / 2), (int) y - (EXTERIOR_TRIANGLE_OFFSET / 2),
						(int) x, (int) y - EXTERIOR_TRIANGLE_OFFSET);
				graphics.drawLine((int) x + (INTERIOR_TRIANGLE_OFFSET / 2), (int) y - (INTERIOR_TRIANGLE_OFFSET / 2),
						(int) x, (int) y - INTERIOR_TRIANGLE_OFFSET);
				graphics.drawRect((int) x - 2, (int) y - LINE_LEN - 2, 4, 4);
				break;
			case RIGHT:
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawLine((int) x, (int) y, (int) x + LINE_LEN, (int) y);
				setColorByDimension(graphics, xDimension);
				graphics.drawLine((int) x + EXTERIOR_TRIANGLE_OFFSET, (int) y, (int) x + (EXTERIOR_TRIANGLE_OFFSET / 2),
						(int) y - (EXTERIOR_TRIANGLE_OFFSET / 2));
				graphics.drawLine((int) x + INTERIOR_TRIANGLE_OFFSET, (int) y, (int) x + (INTERIOR_TRIANGLE_OFFSET / 2),
						(int) y - (INTERIOR_TRIANGLE_OFFSET / 2));
				graphics.drawRect(((int) x + LINE_LEN) - 2, (int) y - 2, 4, 4);
				setColorByDimension(graphics, yDimension);
				graphics.drawLine((int) x, (int) y, (int) x, (int) y - LINE_LEN);
				graphics.drawLine((int) x + (EXTERIOR_TRIANGLE_OFFSET / 2), (int) y - (EXTERIOR_TRIANGLE_OFFSET / 2),
						(int) x, (int) y - EXTERIOR_TRIANGLE_OFFSET);
				graphics.drawLine((int) x + (INTERIOR_TRIANGLE_OFFSET / 2), (int) y - (INTERIOR_TRIANGLE_OFFSET / 2),
						(int) x, (int) y - INTERIOR_TRIANGLE_OFFSET);
				graphics.drawRect((int) x - 2, (int) y - LINE_LEN - 2, 4, 4);
				break;
			case NONE:
				recycleXPoints[0] = (int) x;
				recycleYPoints[0] = (int) y;
				recycleXPoints[1] = (int) x + INTERIOR_TRIANGLE_OFFSET;
				recycleYPoints[1] = (int) y;
				recycleXPoints[2] = (int) x;
				recycleYPoints[2] = (int) y - INTERIOR_TRIANGLE_OFFSET;
				graphics.setColor(new Color(255, 255, 0, 70));
				graphics.fillPolygon(recycleXPoints, recycleYPoints, 3);
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawPolygon(recycleXPoints, recycleYPoints, 3);
				graphics.drawLine((int) x + EXTERIOR_TRIANGLE_OFFSET, (int) y, (int) x,
						(int) y - EXTERIOR_TRIANGLE_OFFSET);
				setColorByDimension(graphics, xDimension);
				graphics.drawLine((int) x, (int) y, (int) x + LINE_LEN, (int) y);
				graphics.drawRect(((int) x + LINE_LEN) - 2, (int) y - 2, 4, 4);
				setColorByDimension(graphics, yDimension);
				graphics.drawLine((int) x, (int) y, (int) x, (int) y - LINE_LEN);
				graphics.drawRect((int) x - 2, (int) y - LINE_LEN - 2, 4, 4);
				break;
			case FLAT_XY:
				recycleXPoints4[0] = (int) x + EXTERIOR_TRIANGLE_OFFSET;
				recycleYPoints4[0] = (int) y;
				recycleXPoints4[1] = (int) x + INTERIOR_TRIANGLE_OFFSET;
				recycleYPoints4[1] = (int) y;
				recycleXPoints4[2] = (int) x;
				recycleYPoints4[2] = (int) y - INTERIOR_TRIANGLE_OFFSET;
				recycleXPoints4[3] = (int) x;
				recycleYPoints4[3] = (int) y - EXTERIOR_TRIANGLE_OFFSET;
				graphics.setColor(new Color(255, 255, 0, 70));
				graphics.fillPolygon(recycleXPoints4, recycleYPoints4, 4);
				graphics.setColor(new Color(255, 255, 0));
				graphics.drawPolygon(recycleXPoints4, recycleYPoints4, 4);
				setColorByDimension(graphics, xDimension);
				graphics.drawLine((int) x, (int) y, (int) x + LINE_LEN, (int) y);
				graphics.drawRect(((int) x + LINE_LEN) - 2, (int) y - 2, 4, 4);
				setColorByDimension(graphics, yDimension);
				graphics.drawLine((int) x, (int) y, (int) x, (int) y - LINE_LEN);
				graphics.drawRect((int) x - 2, (int) y - LINE_LEN - 2, 4, 4);
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

	public enum ScaleDirection {
		UP, RIGHT, FLAT_XY, XYZ, NONE
	}
}
