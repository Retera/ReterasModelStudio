package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.Mat4;
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

	public MoveDimension getDirectionByMouse(Vec2 mousePoint, Mat4 viewportMat, Component parent) {
		MoveDimension direction = MoveDimension.NONE;

		Point mouseP = getMousePoint(mousePoint, parent);

		Vec2 vpPoint = getVpPoint(viewportMat, parent);
		int x = (int) vpPoint.x;
		int y = (int) vpPoint.y;

		if (GU.getTransPolygon(x, y, triangle).contains(mouseP)) {
			return MoveDimension.XYZ;
		}
		if (GU.getTransPolygon(x, y, northLineHitBox).contains(mouseP)) {
			return MoveDimension.Y;
		}
		if (GU.getTransPolygon(x, y, eastLineHitBox).contains(mouseP)) {
			return MoveDimension.X;
		}
		if (GU.getTransPolygon(x, y, romb).contains(mouseP)) {
			return MoveDimension.XY;
		}
		return direction;
	}

	public Vec3 getPoint() {
		return point;
	}

	@Override
	public void render(Graphics2D graphics, Mat4 viewportMat, Mat4 invViewportMat, Component parent) {
		float aspect = parent.getWidth() / (float)parent.getHeight();
		temp0.set(0, 0, 0).transform(invViewportMat, 1, true);

		Vec3 tempX = tempPoint.set(1, 0, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension xDim = MoveDimension.getByAxis(tempX.normalize());

		Vec3 tempY = tempPoint.set(0, aspect*1, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension yDim = MoveDimension.getByAxis(tempY.normalize());

		Vec3 tempTot = tempPoint.set(1, aspect*1, 0).transform(invViewportMat, 1, true).sub(temp0);
		MoveDimension totDim = MoveDimension.getByAxis(tempTot.normalize());

		Vec2 vpPoint = getVpPoint(viewportMat, parent);
		int x = (int) vpPoint.x;
		int y = (int) vpPoint.y;

		if (moveDirection != null) {
			graphics.setColor(getHighLightableColor(xDim, moveDirection == xDim || moveDirection == totDim));
			drawEastLine(graphics, x, y);
			drawDiagonalLineEast(graphics, x, y, EXTERIOR_TRIANGLE_OFFSET);
			drawDiagonalLineEast(graphics, x, y, INTERIOR_TRIANGLE_OFFSET);

			graphics.setColor(getHighLightableColor(yDim, moveDirection == yDim || moveDirection == totDim));
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
