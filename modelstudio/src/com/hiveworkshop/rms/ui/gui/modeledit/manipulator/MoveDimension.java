package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public enum MoveDimension {
	NONE, X, Y, XY, Z, XZ, YZ, XYZ,
	;

	public static MoveDimension getByAxis(Vec3 axis) {
		MoveDimension mdX = 0.5<Math.abs(axis.dot(Vec3.X_AXIS)) ? X : NONE;
		MoveDimension mdY = 0.5<Math.abs(axis.dot(Vec3.Y_AXIS)) ? Y : NONE;
		MoveDimension mdZ = 0.5<Math.abs(axis.dot(Vec3.Z_AXIS)) ? Z : NONE;

		return MoveDimension.values()[mdX.ordinal() | mdY.ordinal() | mdZ.ordinal()];
	}

	public Color getColor() {
		return switch (this) {
			case X -> new Color(0, 255, 0);
			case Y -> new Color(255, 0, 0);
			case Z -> new Color(0, 0, 255);
			default -> new Color(255, 0, 255);
		};
	}

	public boolean containDim(MoveDimension dim) {
		// Some bit magic to se if this contains the dimension (ex Y in XY)
		return (ordinal() & dim.ordinal()) == dim.ordinal();
	}

	public static Vec3 getDirAxis(MoveDimension dir) {
		return switch (dir) {
			case NONE -> null;
			case X  -> new Vec3(1, 0, 0);
			case Y  -> new Vec3(0, 1, 0);
			case XY -> new Vec3(1, 1, 0);
			case Z  -> new Vec3(0, 0, 1);
			case XZ -> new Vec3(1, 0, 1);
			case YZ -> new Vec3(0, 1, 1);
			case XYZ -> new Vec3(1, 1, 1);
		};
	}

//	public static Quat getDirAxisRot(MoveDimension dir) {
//		return switch (dir) {
//			case NONE -> null;
//			case X  -> new Quat(1, 0, 0, 1);
//			case Y  -> new Quat(0, 1, 0, 1);
//			case XY -> new Quat(1, 1, 0, 1);
//			case Z  -> new Quat(0, 0, 1, 1);
//			case XZ -> new Quat(1, 0, 1, 1);
//			case YZ -> new Quat(0, 1, 1, 1);
//			case XYZ -> new Quat(1, 1, 1, 1);
//		};
//	}

}
