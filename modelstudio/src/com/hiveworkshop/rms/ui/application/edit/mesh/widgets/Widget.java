package com.hiveworkshop.rms.ui.application.edit.mesh.widgets;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;


public abstract class Widget {
	protected final Vec3 point = new Vec3(0, 0, 0);
	protected MoveDimension moveDirection = MoveDimension.NONE;

	public abstract MoveDimension getDirectionByMouse(Vec2 mousePoint, CoordinateSystem coordinateSystem);

	public abstract void render(Graphics2D graphics, CoordinateSystem coordinateSystem);

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

	protected void setColorByDimension(Graphics2D graphics, byte dimension) {
		switch (dimension) {
			case 0, -1 -> graphics.setColor(new Color(0, 255, 0));
			case 1, -2 -> graphics.setColor(new Color(255, 0, 0));
			case 2, -3 -> graphics.setColor(new Color(0, 0, 255));
		}
	}

	protected void setHighLightableColor(Graphics2D graphics, byte dimension, MoveDimension moveDimension) {
//		System.out.println(moveDimension + " has " + MoveDimension.getByByte(dimension) + "?");
		if (moveDimension.containDirection(dimension)) {
			graphics.setColor(new Color(255, 255, 0));
		} else {
			setColorByDimension(graphics, dimension);
		}
	}
}
