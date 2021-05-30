package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class ScalerWidget extends Widget {
	private static final int LINE_LEN = 80;
	private static final int LINE_SENS = 3;
	private static final int EXTERIOR_TRIANGLE_OFFSET = LINE_LEN - 16;
	private static final int INTERIOR_TRIANGLE_OFFSET = LINE_LEN - 32;

	private final Polygon triangle;
	private final Polygon romb;

	private final Polygon northLineHitBox;
	private final Polygon eastLineHitBox;

	public ScalerWidget() {
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

	@Override
	public MoveDimension getDirectionByMouse(Vec2 mousePoint1, CoordinateSystem coordinateSystem) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		int x = (int) coordinateSystem.viewX(point.getCoord(dim1));
		int y = (int) coordinateSystem.viewY(point.getCoord(dim2));

		MoveDimension direction = MoveDimension.NONE;

		Point mousePoint = new Point((int) mousePoint1.x, (int) mousePoint1.y);

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

	public Vec3 getPoint() {
		return point;
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		byte xDimension = coordinateSystem.getPortFirstXYZ();
		byte yDimension = coordinateSystem.getPortSecondXYZ();
		int x = (int) coordinateSystem.viewX(point.getCoord(xDimension));
		int y = (int) coordinateSystem.viewY(point.getCoord(yDimension));
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
		GU.drawCenteredSquare(graphics, x, y - LINE_LEN, 4);
		graphics.drawLine(x, y, x, y - LINE_LEN);
	}

	public void drawEastLine(Graphics2D graphics, int x, int y) {
		GU.drawCenteredSquare(graphics, x + LINE_LEN, y, 4);
		graphics.drawLine(x, y, x + LINE_LEN, y);
	}
}
