package com.hiveworkshop.rms.util;

public class InexactHashVector {
	private final float x, y, z;
	public int precision = 1000;

	public InexactHashVector(Vec3 vertex) {
		x = vertex.x;
		y = vertex.y;
		z = vertex.z;
	}

	public InexactHashVector(Vec3 vertex, int precision) {
		x = vertex.x;
		y = vertex.y;
		z = vertex.z;
		this.precision = precision;
	}

	public float[] getValues() {
		return new float[] {x, y, z};
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (x * precision);
		result = prime * result + (int) (y * precision);
		result = prime * result + (int) (z * precision);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InexactHashVector other = (InexactHashVector) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		return Float.floatToIntBits(z) == Float.floatToIntBits(other.z);
	}
}
