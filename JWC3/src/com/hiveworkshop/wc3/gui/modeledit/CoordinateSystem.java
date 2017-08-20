package com.hiveworkshop.wc3.gui.modeledit;

public interface CoordinateSystem {
	double convertX(double x);

	double convertY(double y);

	double geomX(double x);

	double geomY(double y);

	byte getPortFirstXYZ();

	byte getPortSecondXYZ();

	public final class Util {
		public static CoordinateSystem identity(final byte a, final byte b) {
			return new CoordinateSystem() {
				@Override
				public double convertX(final double x) {
					return x;
				}

				@Override
				public double convertY(final double y) {
					return y;
				}

				@Override
				public double geomX(final double x) {
					return x;
				}

				@Override
				public double geomY(final double y) {
					return y;
				}

				@Override
				public byte getPortFirstXYZ() {
					return a;
				}

				@Override
				public byte getPortSecondXYZ() {
					return b;
				}

			};
		}

		public static byte getUnusedXYZ(final CoordinateSystem coordinateSystem) {
			return (byte) (3 - coordinateSystem.getPortFirstXYZ() - coordinateSystem.getPortSecondXYZ());
		}

		private Util() {
		}
	}
}
