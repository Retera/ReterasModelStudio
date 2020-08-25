package com.hiveworkshop.rms.editor.model;

public class Vertex4 {
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public float w = 0;

	public Vertex4() {

	}
	
	public Vertex4(final float x, final float y, final float z, final float w) {
		set(x, y, z, w);
	}

	public Vertex4(final double x, final double y, final double z, final double w) {
		set(x, y, z, w);
	}

	public Vertex4(final Vertex4 v) {
		set(v);
	}

	public Vertex4(final float[] data) {
		set(data[0], data[1], data[2], data[3]);
	}

	public Vertex4(final double[] data) {
		set(data[0], data[1], data[2], data[3]);
	}

	public Vertex4(final float[] data, final boolean flip) {
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

	public void set(final Vertex4 v) {
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

	public boolean equals(final Vertex4 v) {
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

	public float distanceSquared(final Vertex4 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;
		final float dz = a.z - z;
		final float dw = a.w - w;

		return (dx * dx) + (dy * dy) + (dz * dz) + (dw * dw);
	}

	public float distance(final Vertex4 a) {
		return (float) Math.sqrt(distanceSquared(a));
	}

	public Vertex4 delta(final Vertex4 a) {
		return new Vertex4(a.x - x, a.y - y, a.z - z, a.w - w);
	}

	public Vertex4 sub(final Vertex4 a) {
		x -= a.x;
		y -= a.y;
		z -= a.z;
		w -= a.w;

		return this;
	}

	public Vertex4 add(final Vertex4 a) {
		return add(this, a, this);
	}

	public static Vertex4 add(final Vertex4 a, final Vertex4 b, final Vertex4 out) {
		out.x = a.x + b.x;
		out.y = a.y + b.y;
		out.z = a.z + b.z;
		out.w = a.w + b.w;

		return out;
	}

	public float dot(final Vertex4 a) {
		return (x * a.x) + (y * a.y) + (z * a.z) + (w * a.w);
	}

	public Vertex4 scale(final float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		w *= factor;

		return this;
	}

	public Vertex4 scale(final double factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		w *= factor;

		return this;
	}

	public void normalize() {
		float len = lengthSquared();

		if (len > 0f) {
			len = 1f / (float) Math.sqrt(len);
		}
		
		x *= len;
		y *= len;
		z *= len;
		w *= len;
	}
}
