package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class MoverWidget extends Widget {
	private static final int LINE_LONG = 60;
	private static final int LINE_SHORT = 20;
	private static final int TRIANGLE_OFFSET = LINE_LONG - 16;
	private final Polygon northTriangle;
	private final Polygon eastTriangle;

	private final Polygon eastLineHitBox;
	private final Polygon nortLineHitBox;

	public MoverWidget() {
		northTriangle = GU.getSymTriPoly(5, 0, -18);
		northTriangle.translate(0, -TRIANGLE_OFFSET);

		eastTriangle = GU.getSymTriPoly(0, 5, 18);
		eastTriangle.translate(TRIANGLE_OFFSET, 0);

		eastLineHitBox = GU.getRektPoly(0, -1, LINE_LONG, 1);

		nortLineHitBox = GU.getRektPoly(-1, 0, 1, -LINE_LONG);
	}

	private long debugPrintLimiter;

	@Override
	public MoveDimension getDirectionByMouse(Vec2 mousePoint1, CoordinateSystem coordinateSystem) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		double x = coordinateSystem.viewX(point.getCoord(dim1));
		double y = coordinateSystem.viewY(point.getCoord(dim2));
		long currentTime = System.currentTimeMillis();
		if (debugPrintLimiter < currentTime) {
			debugPrintLimiter = currentTime + 500;
//			System.out.println("d1: "  + dim1 + ", d2: " + dim2);
		}

		MoveDimension direction = MoveDimension.NONE;
		Point mousePoint = new Point((int)mousePoint1.x, (int)mousePoint1.y);

		if (GU.getTransPolygon((int) x, (int) y, northTriangle).contains(mousePoint)
				|| GU.getTransPolygon((int) x, (int) y, nortLineHitBox).contains(mousePoint)) {
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

	public Vec3 getPoint() {
		return point;
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		byte xDimension = coordinateSystem.getPortFirstXYZ();
		byte yDimension = coordinateSystem.getPortSecondXYZ();
		int x = (int) coordinateSystem.viewX(point.getCoord(xDimension));
		int y = (int) coordinateSystem.viewY(point.getCoord(yDimension));

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
}
