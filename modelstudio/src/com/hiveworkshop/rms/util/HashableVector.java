package com.hiveworkshop.rms.util;

public class HashableVector {
	private final float x, y, z;

	public HashableVector(Vec3 vertex) {
		x = vertex.x;
		y = vertex.y;
		z = vertex.z;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
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
		HashableVector other = (HashableVector) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		return Float.floatToIntBits(z) == Float.floatToIntBits(other.z);
	}
}
