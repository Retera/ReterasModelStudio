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

	public Vec4(final Vec3 v, final float w) {
		set(v, w);
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

	public static Vec4 getTransformed(final Vec4 a, Mat4 mat4) {
		return new Vec4(a).transform(mat4);
	}

	public static Vec4 getTransformed(final Vec4 a, Quat quat) {
		return new Vec4(a).transform(quat);
	}

	public static Vec4 getDiff(final Vec4 a, final Vec4 b) {
		return new Vec4(a).sub(b);
	}

	public static Vec4 getSum(final Vec4 a, final Vec4 b) {
		return new Vec4(a).add(b);
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
		return new double[] {x, y, z, w};
	}

	public float[] toFloatArray() {
		return new float[] {x, y, z, w};
	}

	public short[] toShortArray() {
		return new short[] {(short) x, (short) y, (short) z, (short) w};
	}

	public long[] toLongArray() {
		return new long[] {(long) x, (long) y, (long) z, (long) w};
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public static Vec4 getScaled(final Vec4 a, final float factor) {
		return new Vec4(a).scale(factor);
	}

//	public float distance(final Vec4 a) {
//		return (float) Math.sqrt(distanceSquared(a));
//	}

	public static Vec4 getNormalized(final Vec4 a) {
		return new Vec4(a).normalize();
	}

	public static Vec4 getLerped(final Vec4 from, final Vec4 toward, final float t) {
		return new Vec4(from).lerp(toward, t);
	}

	public static Vec4 getHermite(final Vec4 from, final Vec4 outTan, final Vec4 inTan, final Vec4 toward, final float t) {
		return new Vec4(from).hermite(outTan, inTan, toward, t);
	}

	public static Vec4 getBezier(final Vec4 from, final Vec4 outTan, final Vec4 inTan, final Vec4 toward, final float t) {
		return new Vec4(from).hermite(outTan, inTan, toward, t);
	}

	public Vec4 transform(Mat4 mat4) {
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + (mat4.m20 * z) + (mat4.m30 * w);
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + (mat4.m21 * z) + (mat4.m31 * w);
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + (mat4.m22 * z) + (mat4.m32 * w);
		float newW = (mat4.m03 * x) + (mat4.m13 * y) + (mat4.m23 * z) + (mat4.m33 * w);

		return set(newX, newY, newZ, newW);
	}

	public Vec4 transform(Quat quat) {
		final float uvx = quat.y * z - quat.z * y;
		final float uvy = quat.z * x - quat.x * z;
		final float uvz = quat.x * y - quat.y * x;
		final float uuvx = quat.y * uvz - quat.z * uvy;
		final float uuvy = quat.z * uvx - quat.x * uvz;
		final float uuvz = quat.x * uvy - quat.y * uvx;
		final float w2 = quat.w * 2;

		float newX = x + (uvx * w2) + (uuvx * 2);
		float newY = y + (uvy * w2) + (uuvy * 2);
		float newZ = z + (uvz * w2) + (uuvz * 2);
		float newW = w;

		return set(newX, newY, newZ, newW);
	}

	public float dot(final Vec4 a) {
		return (x * a.x) + (y * a.y) + (z * a.z) + (w * a.w);
	}

	public float distanceSquared(final Vec4 a) {

		final float dx = a.x - x;
		final float dy = a.y - y;
		final float dz = a.z - z;
		final float dw = a.w - w;

		return (dx * dx) + (dy * dy) + (dz * dz) + (dw * dw);
	}

	public float distance(final Vec4 a) {
		return getDiff(this, a).length();
	}

	public Vec4 sub(final Vec4 a) {
		x = x - a.x;
		y = y - a.y;
		z = z - a.z;
		w = w - a.w;

		return this;
	}

	public Vec4 add(final Vec4 a) {
		x = x + a.x;
		y = y + a.y;
		z = z + a.z;
		w = w + a.w;
		return this;
	}

	public Vec4 add(final Vec3 a) {
		x = x + a.x;
		y = y + a.y;
		z = z + a.z;
		return this;
	}

	public Vec4 divide(final Vec4 a) {
		x = x / a.x;
		y = y / a.y;
		z = z / a.z;
		w = w / a.w;
		return this;
	}

	public Vec4 scale(final float factor) {
		x = x * factor;
		y = y * factor;
		z = z * factor;
		w = w * factor;
		return this;
	}

	public Vec4 multiply(Vec4 a) {
		x = x * a.x;
		y = y * a.y;
		z = z * a.z;
		w = w * a.w;
		return this;
	}

	public Vec4 normalize() {
		float len = lengthSquared();

		if (len > 0) {
			len = 1 / (float) Math.sqrt(len);
		}
		return scale(len);
	}

	public Vec4 lerp(final Vec4 toward, final float t) {
		x = MathUtils.lerp(x, toward.x, t);
		y = MathUtils.lerp(y, toward.y, t);
		z = MathUtils.lerp(z, toward.z, t);
		w = MathUtils.lerp(w, toward.w, t);
		return this;
	}

	public Vec4 hermite(final Vec4 outTan, final Vec4 inTan, final Vec4 toward, final float t) {
		final float factorTimes2 = t * t;
		final float factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final float factor2 = (factorTimes2 * (t - 2)) + t;
		final float factor3 = factorTimes2 * (t - 1);
		final float factor4 = factorTimes2 * (3 - (2 * t));

		x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (toward.x * factor4);
		y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (toward.y * factor4);
		z = (z * factor1) + (outTan.z * factor2) + (inTan.z * factor3) + (toward.z * factor4);
		return this;
	}

	public Vec4 bezier(final Vec4 outTan, final Vec4 inTan, final Vec4 toward, final float t) {
		final float invt = 1 - t;
		final float factorSquared = t * t;
		final float inverseFactorSquared = invt * invt;
		final float factor1 = inverseFactorSquared * invt;
		final float factor2 = 3 * t * inverseFactorSquared;
		final float factor3 = 3 * factorSquared * invt;
		final float factor4 = factorSquared * t;

		x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (toward.x * factor4);
		y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (toward.y * factor4);
		z = (z * factor1) + (outTan.z * factor2) + (inTan.z * factor3) + (toward.z * factor4);
		w = (w * factor1) + (outTan.w * factor2) + (inTan.w * factor3) + (toward.w * factor4);
		return this;
	}

	public boolean isValid() {
		return !(Float.isNaN(this.x)
				|| Float.isNaN(this.y)
				|| Float.isNaN(this.z)
				|| Float.isNaN(this.w)
				|| Float.isInfinite(this.x)
				|| Float.isInfinite(this.y)
				|| Float.isInfinite(this.z)
				|| Float.isInfinite(this.w));
	}

	public Vec3 getVec3() {
		return new Vec3(x, y, z);
	}

	public Vec4 set(final Vec3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public Vec4 set(final Vec3 v, float w) {
		x = v.x;
		y = v.y;
		z = v.z;
		this.w = w;
		return this;
	}

	public Vec4 set(final Vec4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		w = v.w;
		return this;
	}

	public Vec4 set(final float vx, final float vy, final float vz, final float vw) {
		x = vx;
		y = vy;
		z = vz;
		w = vw;
		return this;
	}

	public Vec4 set(final double vx, final double vy, final double vz, final double vw) {
		x = (float) vx;
		y = (float) vy;
		z = (float) vz;
		w = (float) vw;
		return this;
	}

	public Vec4 set(final float[] a) {
		x = a[0];
		y = a[1];
		z = a[2];
		w = a[3];
		return this;
	}
}
