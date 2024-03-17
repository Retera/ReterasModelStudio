package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;


public abstract class Widget {
	protected Component parent;
	protected final Vec3 point = new Vec3(0, 0, 0);
	protected final Vec2 vpPoint = new Vec2(0, 0);
	protected final Vec3 temp0 = new Vec3(0, 0, 0);
	protected final Vec3 tempPoint = new Vec3();
	protected MoveDimension moveDirection = MoveDimension.NONE;

	public MoveDimension getDirectionByMouse(Vec2 mousePoint, Mat4 viewportMat, Component parent){
		return MoveDimension.NONE;
	}

	public void render(Graphics2D graphics, Mat4 viewportMat, Mat4 invViewportMat, Component parent){
	}

	protected Vec2 getVpPoint(Mat4 viewportMat, Component parent) {
		tempPoint.set(point).transform(viewportMat, 1, true);
		int x = MathUtils.clamp((int) ((1 + tempPoint.x)/2f * parent.getWidth()), 0, parent.getWidth()-25);
		int y = MathUtils.clamp((int) ((1 - tempPoint.y)/2f * parent.getHeight()), 25, parent.getHeight());
		return vpPoint.set(x, y);
	}

//	protected Vec2 getVpPoint(Mat4 viewportMat, Component parent){
//		tempPoint.set(point).transform(viewportMat, 1, true);
//		int x = (int) ((1 + tempPoint.x)/2f * parent.getWidth());
//		int y = (int) ((1 - tempPoint.y)/2f * parent.getHeight());
//		return vpPoint.set(x, y);
//	}

	protected Point getMousePoint(Vec2 mousePoint, Component parent) {
		float x = (1 + mousePoint.x) / 2f * parent.getWidth();
		float y = (1 - mousePoint.y) / 2f * parent.getHeight();
		return new Point((int) x, (int) y);
	}

	public void setPoint(Vec3 point) {
		this.point.set(point);
	}

	public void setPoint(Vec2 point) {
		this.point.set(point.x, point.y, 0);
	}

	public MoveDimension getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(MoveDimension moveDirection) {
		this.moveDirection = moveDirection;
	}

	protected Color getColor(MoveDimension moveDimension) {
		return switch (moveDimension) {
			case X -> new Color(0, 255, 0);
			case Y -> new Color(255, 0, 0);
			case Z -> new Color(0, 0, 255);
			default -> new Color(255, 0, 255);
		};
	}
	protected Color getHighLightableColor(MoveDimension moveDimension, boolean highlight) {
		if (highlight) {
			return new Color(255, 255, 0);
		} else {
			return getColor(moveDimension);
		}
	}
}
