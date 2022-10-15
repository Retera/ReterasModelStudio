package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Vec3;

public class ViewBox {
	Plane bottom    = new Plane(); // bottom
	Plane right     = new Plane(); // right
	Plane top       = new Plane(); // top
	Plane left      = new Plane(); // left
//	Plane far        = new Plane(); // far
//	Plane near       = new Plane(); // near

	public ViewBox() {

	}

	public ViewBox set(Vec3 topLeft, Vec3 bottomRight, Vec3 topNorm, Vec3 botNorm, Vec3 leftNorm, Vec3 rightNorm) {
		bottom.set(botNorm, bottomRight);
		left.set(leftNorm, topLeft);
		top.set(topNorm, topLeft);
		right.set(rightNorm, bottomRight);
		return this;
	}

	public ViewBox setTop(Vec3 pos, Vec3 norm) {
		top.set(norm, pos);
		return this;
	}

	public ViewBox setBot(Vec3 pos, Vec3 norm) {
		bottom.set(norm, pos);
		return this;
	}

	public ViewBox setLeft(Vec3 pos, Vec3 norm) {
		left.set(norm, pos);
		return this;
	}

	public ViewBox setRight(Vec3 pos, Vec3 norm) {
		right.set(norm, pos);
		return this;
	}

	public boolean pointInBox(Vec3 point) {
		return     bottom.inFrontOf(point)
				&& right.inFrontOf(point)
				&& top.inFrontOf(point)
				&& left.inFrontOf(point);
	}

	public boolean pointInBox(Vec3 point, float margin) {
		return     bottom.inFrontOf(point, margin)
				&& right.inFrontOf(point, margin)
				&& top.inFrontOf(point, margin)
				&& left.inFrontOf(point, margin);
	}

	public boolean pointInBoxPrint(Vec3 point) {
		boolean b_bottom = bottom.inFrontOf(point);
		boolean b_right = right.inFrontOf(point);
		boolean b_top = top.inFrontOf(point);
		boolean b_left = left.inFrontOf(point);
		System.out.println(""
				+ "in front of Bottom: " + b_bottom + "\t( " + bottom.getPoint() + ",\t" + bottom.getNorm() + " )"
				+ "\nin front of Right:  " + b_right + "\t( " + right.getPoint() + ",\t" + right.getNorm() + " )"
				+ "\nin front of Top:    " + b_top + "\t( " + top.getPoint() + ",\t" + top.getNorm() + " )"
				+ "\nin front of Left:   " + b_left + "\t( " + left.getPoint() + ",\t" + left.getNorm() + " )"
		);
		return b_bottom && b_right && b_top && b_left;
	}
}
