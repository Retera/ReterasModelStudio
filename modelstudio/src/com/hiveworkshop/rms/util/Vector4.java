package com.hiveworkshop.rms.util;

public class Vector4 {
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public float w = 0;

	public Vector4() {

	}
	
	public Vector4(final float x, final float y, final float z, final float w) {
		set(x, y, z, w);
	}

	public Vector4(final double x, final double y, final double z, final double w) {
		set(x, y, z, w);
	}

	public Vector4(final Vector4 v) {
		set(v);
	}

	public Vector4(final float[] data) {
		set(data[0], data[1], data[2], data[3]);
	}

	public Vector4(final double[] data) {
		set(data[0], data[1], data[2], data[3]);
	}

	public Vector4(final float[] data, final boolean flip) {
		if (flip) {
			z = data[0];
			y = data[1];
			x = data[2];
			w = data[3];
		} else {
			x = data[0];
			y = data[1];
			z = data[2];
			w = data[3];
		}
	}

	public float getCoord(final byte dim) {
		switch (dim) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		case 3:
			return w;
		}
		return 0;
	}

	public void setCoord(final byte dim, final float value) {
		if (!Float.isNaN(value)) {
			switch (dim) {
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			case 2:
				z = value;
				break;
			case 3:
				w = value;
				break;
			}
		}
	}

	public void setCoord(final byte dim, final double value) {
		if (!Double.isNaN(value)) {
			switch (dim) {
			case 0:
				x = (float) value;
				break;
			case 1:
				y = (float) value;
				break;
			case 2:
				z = (float) value;
				break;
			case 3:
				w = (float) value;
				break;
			}
		}
	}

	public void translateCoord(final byte dim, final float value) {
		switch (dim) {
		case 0:
			x += value;
			break;
		case 1:
			y += value;
			break;
		case 2:
			z += value;
			break;
		case 3:
			w += value;
			break;
		}
	}

	public void set(final Vector4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		w = v.w;
	}

	public void set(final float vx, final float vy, final float vz, final float vw) {
		x = vx;
		y = vy;
		z = vz;
		w = vw;
	}

	public void set(final double vx, final double vy, final double vz, final double vw) {
		x = (float) vx;
		y = (float) vy;
		z = (float) vz;
		w = (float) vw;
	}

	public boolean equals(final Vector4 v) {
		return (x == v.x) && (y == v.y) && (z == v.z) && (w == v.w);
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + ", " + w + "}";
	}

	public String toStringLessSpace() {
		return "{" + x + ", " + y + ", " + z + ", " + w + "}";
	}

	public double[] toArray() {
		return new double[] { x, y, z, w };
	}

	public float[] toFloatArray() {
		return new float[] { x,  y, z, w };
	}

	public short[] toShortArray() {
		return new short[] { (short)x, (short)y, (short)z, (short)w };
	}

	public long[] toLongArray() {
		return new long[] { (long)x, (long)y, (long)z, (long)w };
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float distanceSquared(final Vector4 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;
		final float dz = a.z - z;
		final float dw = a.w - w;

		return (dx * dx) + (dy * dy) + (dz * dz) + (dw * dw);
	}

	public float distance(final Vector4 a) {
		return (float) Math.sqrt(distanceSquared(a));
	}

	public Vector4 sub(final Vector4 a) {
		x -= a.x;
		y -= a.y;
		z -= a.z;
		w -= a.w;

		return this;
	}

	public Vector4 add(final Vector4 a, final Vector4 out) {
		out.x = x + a.x;
		out.y = y + a.y;
		out.z = z + a.z;
		out.w = w + a.w;

		return out;
	}

	public Vector4 add(final Vector4 a) {
		return add(a, this);
	}

	public float dot(final Vector4 a) {
		return (x * a.x) + (y * a.y) + (z * a.z) + (w * a.w);
	}

	public Vector4 scale(final float factor, final Vector4 out) {
		out.x = x * factor;
		out.y = y * factor;
		out.z = z * factor;
		out.w = w * factor;

		return out;
	}

	public Vector4 scale(final float factor) {
		return scale(factor, this);
	}

	public Vector4 normalize(final Vector4 out) {
		float len = lengthSquared();

		if (len > 0) {
			len = 1 / (float) Math.sqrt(len);
		}
		
		out.x = x * len;
		out.y = y * len;
		out.z = z * len;
		out.w = w * len;

		return out;
	}

	public Vector4 normalize() {
		return normalize(this);
	}
}
