package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.Mat4;
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

	public MoveDimension getDirectionByMouse(Vec2 mousePoint, Mat4 viewportMat, Component parent){
		Vec2 vpPoint = getVpPoint(viewportMat, parent);
		int x = (int) vpPoint.x;
		int y = (int) vpPoint.y;
		long currentTime = System.currentTimeMillis();
		if (debugPrintLimiter < currentTime) {
			debugPrintLimiter = currentTime + 500;
		}

		MoveDimension direction = MoveDimension.NONE;
		Point mouseP = getMousePoint(mousePoint, parent);

		if (GU.getTransPolygon(x, y, northTriangle).contains(mouseP)
				|| GU.getTransPolygon(x, y, nortLineHitBox).contains(mouseP)) {
			direction = MoveDimension.Y;
		}
		if (GU.getTransPolygon(x, y, eastTriangle).contains(mouseP)
				|| GU.getTransPolygon(x, y, eastLineHitBox).contains(mouseP)) {
			direction = MoveDimension.X;
		}
		if (new Rectangle(x, y - LINE_SHORT, LINE_SHORT, LINE_SHORT).contains(mouseP)) {
			direction = MoveDimension.XY;
		}
		return direction;
	}

	public Vec3 getPoint() {
		return point;
	}

	public void render(Graphics2D graphics, Mat4 viewportMat, Mat4 invViewportMat, Component parent){
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

		graphics.setColor(getHighLightableColor(yDim, moveDirection == yDim || moveDirection == totDim));
		drawNorthArrow(graphics, x, y);

		graphics.setColor(getHighLightableColor(xDim, moveDirection == xDim || moveDirection == totDim));
		drawEastArrow(graphics, x, y);

		graphics.setColor(getColor(xDim));
		drawNorthLine(graphics, x, y, LINE_SHORT, LINE_SHORT);

		graphics.setColor(getColor(yDim));
		drawEastLine(graphics, x, y, LINE_SHORT, LINE_SHORT);

		if (moveDirection == totDim) {
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
