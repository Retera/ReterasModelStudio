package com.hiveworkshop.rms.util;

import java.awt.*;

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

	public Vec4 setAsAxisWithAngle(Quat quat){
		float angle = (float) Math.acos(MathUtils.clamp(quat.w, -1f, 1f)) * 2;
		float sinOfHalfAngle = (float) Math.sin(angle / 2.0);
		if(sinOfHalfAngle != 0) {
			float ax = quat.x / sinOfHalfAngle;
			float ay = quat.y / sinOfHalfAngle;
			float az = quat.z / sinOfHalfAngle;
			return set(ax, ay, az, angle);
		}
		return set(0, 0, 1, angle);
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
	public boolean equals(Object v) {
		if(v instanceof Vec4){
			return equals((Vec4) v);
		}
		return false;
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + ", " + w + " }";
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
	public float v3Length() {
		return (float) Math.sqrt((x * x) + (y * y) + (z * z));
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

	public Vec4 transform(Mat4 mat4, float w) {
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + (mat4.m20 * z) + (mat4.m30 * w);
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + (mat4.m21 * z) + (mat4.m31 * w);
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + (mat4.m22 * z) + (mat4.m32 * w);

		return set(newX, newY, newZ, this.w);
	}
	public Vec4 transformInverted(Mat4 mat4) {
		float b00 = mat4.m00 * mat4.m11 - mat4.m01 * mat4.m10;
		float b01 = mat4.m00 * mat4.m12 - mat4.m02 * mat4.m10;
		float b02 = mat4.m00 * mat4.m13 - mat4.m03 * mat4.m10;
		float b03 = mat4.m01 * mat4.m12 - mat4.m02 * mat4.m11;
		float b04 = mat4.m01 * mat4.m13 - mat4.m03 * mat4.m11;
		float b05 = mat4.m02 * mat4.m13 - mat4.m03 * mat4.m12;
		float b06 = mat4.m20 * mat4.m31 - mat4.m21 * mat4.m30;
		float b07 = mat4.m20 * mat4.m32 - mat4.m22 * mat4.m30;
		float b08 = mat4.m20 * mat4.m33 - mat4.m23 * mat4.m30;
		float b09 = mat4.m21 * mat4.m32 - mat4.m22 * mat4.m31;
		float b10 = mat4.m21 * mat4.m33 - mat4.m23 * mat4.m31;
		float b11 = mat4.m22 * mat4.m33 - mat4.m23 * mat4.m32;

		// Calculate the determinant
		float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;

		if (det == 0f) {
			return null;
		}

		det = 1f / det;

		float tmp00 = (mat4.m11 * b11) - (mat4.m12 * b10) + (mat4.m13 * b09) * det;
		float tmp01 = (mat4.m02 * b10) - (mat4.m01 * b11) - (mat4.m03 * b09) * det;
		float tmp02 = (mat4.m31 * b05) - (mat4.m32 * b04) + (mat4.m33 * b03) * det;
		float tmp03 = (mat4.m22 * b04) - (mat4.m21 * b05) - (mat4.m23 * b03) * det;
		float tmp10 = (mat4.m12 * b08) - (mat4.m10 * b11) - (mat4.m13 * b07) * det;
		float tmp11 = (mat4.m00 * b11) - (mat4.m02 * b08) + (mat4.m03 * b07) * det;
		float tmp12 = (mat4.m32 * b02) - (mat4.m30 * b05) - (mat4.m33 * b01) * det;
		float tmp13 = (mat4.m20 * b05) - (mat4.m22 * b02) + (mat4.m23 * b01) * det;
		float tmp20 = (mat4.m10 * b10) - (mat4.m11 * b08) + (mat4.m13 * b06) * det;
		float tmp21 = (mat4.m01 * b08) - (mat4.m00 * b10) - (mat4.m03 * b06) * det;
		float tmp22 = (mat4.m30 * b04) - (mat4.m31 * b02) + (mat4.m33 * b00) * det;
		float tmp23 = (mat4.m21 * b02) - (mat4.m20 * b04) - (mat4.m23 * b00) * det;
		float tmp30 = (mat4.m11 * b07) - (mat4.m10 * b09) - (mat4.m12 * b06) * det;
		float tmp31 = (mat4.m00 * b09) - (mat4.m01 * b07) + (mat4.m02 * b06) * det;
		float tmp32 = (mat4.m31 * b01) - (mat4.m30 * b03) - (mat4.m32 * b00) * det;
		float tmp33 = (mat4.m20 * b03) - (mat4.m21 * b01) + (mat4.m22 * b00) * det;

		float newX = (tmp00 * x) + (tmp10 * y) + (tmp20 * z) + (tmp30 * w);
		float newY = (tmp01 * x) + (tmp11 * y) + (tmp21 * z) + (tmp31 * w);
		float newZ = (tmp02 * x) + (tmp12 * y) + (tmp22 * z) + (tmp32 * w);
		float newW = (tmp03 * x) + (tmp13 * y) + (tmp23 * z) + (tmp33 * w);

		return set(newX, newY, newZ, newW);
	}

	public Vec4 transform(Quat quat) {
		float uvx = quat.y * z - quat.z * y;
		float uvy = quat.z * x - quat.x * z;
		float uvz = quat.x * y - quat.y * x;
		float uuvx = quat.y * uvz - quat.z * uvy;
		float uuvy = quat.z * uvx - quat.x * uvz;
		float uuvz = quat.x * uvy - quat.y * uvx;
		float w2 = quat.w * 2;

		x += (uvx * w2) + (uuvx * 2);
		y += (uvy * w2) + (uuvy * 2);
		z += (uvz * w2) + (uuvz * 2);

		return this;
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

	public Vec4 sub(final Vec3 a) {
		x = x - a.x;
		y = y - a.y;
		z = z - a.z;

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

	public Vec4 addScaled(Vec4 vec, float scale) {
		x += scale * vec.x;
		y += scale * vec.y;
		z += scale * vec.z;
		w += scale * vec.w;
		return this;
	}

	public Vec4 addScaled(Vec3 vec, float scale) {
		x += scale * vec.x;
		y += scale * vec.y;
		z += scale * vec.z;
		return this;
	}

	public Vec4 normalize() {
		float len = lengthSquared();

		if (len > 0) {
			len = 1 / (float) Math.sqrt(len);
		}
		return scale(len);
	}

	public Vec4 normalizeAsV3() {
		float len = (x * x) + (y * y) + (z * z);
		float w = this.w;

		if (len > 0) {
			len = 1 / (float) Math.sqrt(len);
		}
		scale(len);
		this.w = w;
		return this;
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



	public Color asIntColor() {
//		float toInt = 0<=x && x<=1 && 0<=y && y<=1 && 0<=z && z<=1 &&
		float max = Math.max(x, Math.max(y, z));
		float scale = max <= 255 ? 1 : 255 / max;
		int red = (int) (x * scale);
		int green = (int) (y * scale);
		int blue = (int) (z * scale);
		int alpha = (int) (x * scale);
		return new Color(red, green, blue, alpha);
	}
	public Color asFloatColor() {
		float max = Math.max(x, Math.max(y, z));
		float scale = max <= 1 ? 1 : 1 / max;
		float red = x * scale;
		float green = y * scale;
		float blue = z * scale;
		float alpha = x * scale;
		return new Color(red, green, blue, alpha);
	}

	public static Vec4 valueOf(String s) throws NumberFormatException {
		return parseVec4(s);
	}

	public static Vec4 parseVec4(String s) throws NumberFormatException {
		String unbracketed = s.replaceAll("[\\[\\](){}]", "");
		String[] numbers = unbracketed.split(",");
		float num0 = Float.parseFloat(numbers[0].strip());
		float num1 = Float.parseFloat(numbers[1].strip());
		float num2 = Float.parseFloat(numbers[2].strip());
		float num3 = Float.parseFloat(numbers[3].strip());
		return new Vec4(num0, num1, num2, num3);
	}

}
