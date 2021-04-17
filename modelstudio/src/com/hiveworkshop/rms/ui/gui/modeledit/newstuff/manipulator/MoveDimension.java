package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

public enum MoveDimension {
	NONE, X, Y, XY, Z, XZ, YZ, XYZ;

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
}
