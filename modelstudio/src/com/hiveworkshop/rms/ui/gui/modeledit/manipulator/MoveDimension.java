package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public enum MoveDimension {
	NONE, X, Y, XY, Z, XZ, YZ, XYZ,
	;

	public static MoveDimension getByByte(byte dimension) {
		return switch (dimension) {
			case 0, -1 -> MoveDimension.X;
			case 1, -2 -> MoveDimension.Y;
			case 2, -3 -> MoveDimension.Z;
			default -> MoveDimension.NONE;
		};
	}

	public static MoveDimension getByByte(final byte dimension, final byte dimension2) {
		MoveDimension d1 = getByByte(dimension);
		MoveDimension d2 = getByByte(dimension2);
		return MoveDimension.values()[d1.ordinal() | d2.ordinal()];
	}

	public boolean containDirection(byte dim) {
		// Some bit magic to se if this contains the dimension (ex Y in XY)
		return (this.ordinal() & getByByte(dim).ordinal()) > 0;
	}

	public static Vec3 getDirAxis(MoveDimension dir) {
		return switch (dir) {
			case NONE -> null;
			case X -> new Vec3(1, 0, 0);
			case Y -> new Vec3(0, 1, 0);
			case XY -> new Vec3(1, 1, 0);
			case Z -> new Vec3(0, 0, 1);
			case XZ -> new Vec3(1, 0, 1);
			case YZ -> new Vec3(0, 1, 1);
			case XYZ -> new Vec3(1, 1, 1);
		};
	}

	public static Quat getDirAxisRot(MoveDimension dir) {
		return switch (dir) {
			case NONE -> null;
			case X -> new Quat(1, 0, 0, 1);
			case Y -> new Quat(0, 1, 0, 1);
			case XY -> new Quat(1, 1, 0, 1);
			case Z -> new Quat(0, 0, 1, 1);
			case XZ -> new Quat(1, 0, 1, 1);
			case YZ -> new Quat(0, 1, 1, 1);
			case XYZ -> new Quat(1, 1, 1, 1);
		};
	}

}
