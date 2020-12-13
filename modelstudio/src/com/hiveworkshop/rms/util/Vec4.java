package com.hiveworkshop.rms.util;

public class Vec4 {
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public float w = 0;

	public Vec4() {

	}
	
	public Vec4(final float x, final float y, final float z, final float w) {
		set(x, y, z, w);
	}

	public Vec4(final double x, final double y, final double z, final double w) {
		set(x, y, z, w);
	}

	public Vec4(final Vec4 v) {
		set(v);
	}

	public Vec4(final float[] data) {
		set(data);
	}

	public Vec4(final double[] data) {
		set(data[0], data[1], data[2], data[3]);
	}

	public Vec4(final float[] data, final boolean flip) {
		if (flip) {
			z = data[0];
			y = data[1];
			x = data[2];
		} else {
			x = data[0];
			y = data[1];
			z = data[2];
		}
		w = data[3];
	}

	public float getCoord(final byte dim) {
		return switch (dim) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			case 3 -> w;
			default -> 0;
		};
	}

	public void setCoord(final byte dim, final float value) {
		if (!Float.isNaN(value)) {
			switch (dim) {
				case 0 -> x = value;
				case 1 -> y = value;
				case 2 -> z = value;
				case 3 -> w = value;
			}
		}
	}

	public void setCoord(final byte dim, final double value) {
		if (!Double.isNaN(value)) {
			switch (dim) {
				case 0 -> x = (float) value;
				case 1 -> y = (float) value;
				case 2 -> z = (float) value;
				case 3 -> w = (float) value;
			}
		}
	}

	public void translateCoord(final byte dim, final float value) {
		switch (dim) {
			case 0 -> x += value;
			case 1 -> y += value;
			case 2 -> z += value;
			case 3 -> w += value;
		}
	}

	public void set(final Vec4 v) {
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

	public void set(final float[] a) {
		x = a[0];
		y = a[1];
		z = a[2];
		w = a[3];
	}

	public boolean equals(final Vec4 v) {
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

	public float distanceSquared(final Vec4 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;
		final float dz = a.z - z;
		final float dw = a.w - w;

		return (dx * dx) + (dy * dy) + (dz * dz) + (dw * dw);
	}

	public float distance(final Vec4 a) {
		return (float) Math.sqrt(distanceSquared(a));
	}

	public Vec4 sub(final Vec4 a) {
		x -= a.x;
		y -= a.y;
		z -= a.z;
		w -= a.w;

		return this;
	}

	public Vec4 add(final Vec4 a, final Vec4 out) {
		out.x = x + a.x;
		out.y = y + a.y;
		out.z = z + a.z;
		out.w = w + a.w;

		return out;
	}

	public Vec4 add(final Vec4 a) {
		return add(a, this);
	}

	public float dot(final Vec4 a) {
		return (x * a.x) + (y * a.y) + (z * a.z) + (w * a.w);
	}

	public Vec4 scale(final float factor, final Vec4 out) {
		out.x = x * factor;
		out.y = y * factor;
		out.z = z * factor;
		out.w = w * factor;

		return out;
	}

	public Vec4 scale(final float factor) {
		return scale(factor, this);
	}

	public Vec4 normalize(final Vec4 out) {
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

	public Vec4 normalize() {
		return normalize(this);
	}

	public Vec4 lerp(final Vec4 a, final float t, final Vec4 out) {
		out.x = MathUtils.lerp(x, a.x, t);
		out.y = MathUtils.lerp(y, a.y, t);
		out.z = MathUtils.lerp(z, a.z, t);
		out.w = MathUtils.lerp(w, a.w, t);

		return out;
	}

	public Vec4 lerp(final Vec4 a, final float t) {
		return lerp(a, t, this);
	}

	public Vec4 hermite(final Vec4 outTan, final Vec4 inTan, final Vec4 a, final float t, final Vec4 out) {
		final float factorTimes2 = t * t;
		final float factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final float factor2 = (factorTimes2 * (t - 2)) + t;
		final float factor3 = factorTimes2 * (t - 1);
		final float factor4 = factorTimes2 * (3 - (2 * t));
		
		out.x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (a.x * factor4);
		out.y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (a.y * factor4);
		out.z = (z * factor1) + (outTan.z * factor2) + (inTan.z * factor3) + (a.z * factor4);

		return out;
	}

	public Vec4 hermite(final Vec4 outTan, final Vec4 inTan, final Vec4 a, final float t) {
		return hermite(outTan, inTan, a, t, this);
	}

	public Vec4 bezier(final Vec4 outTan, final Vec4 inTan, final Vec4 a, final float t, final Vec4 out) {
		final float invt = 1 - t;
		final float factorSquared = t * t;
		final float inverseFactorSquared = invt * invt;
		final float factor1 = inverseFactorSquared * invt;
		final float factor2 = 3 * t * inverseFactorSquared;
		final float factor3 = 3 * factorSquared * invt;
		final float factor4 = factorSquared * t;
		
		out.x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (a.x * factor4);
		out.y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (a.y * factor4);
		out.z = (z * factor1) + (outTan.z * factor2) + (inTan.z * factor3) + (a.z * factor4);
		out.w = (w * factor1) + (outTan.w * factor2) + (inTan.w * factor3) + (a.w * factor4);

		return out;
	}

	public Vec4 bezier(final Vec4 outTan, final Vec4 inTan, final Vec4 a, final float t) {
		return bezier(outTan, inTan, a, t, this);
	}
}
