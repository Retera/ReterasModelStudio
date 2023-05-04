package com.hiveworkshop.rms.util;

import java.awt.*;
import java.util.Collection;

public class Vec3 {
	public static final Vec3 ZERO = new Vec3();
	public static final Vec3 ONE = new Vec3(1,1,1);
	public static final Vec3 X_AXIS = new Vec3(1, 0, 0);
	public static final Vec3 Y_AXIS = new Vec3(0, 1, 0);
	public static final Vec3 Z_AXIS = new Vec3(0, 0, 1);
	public static final Vec3 NEGATIVE_X_AXIS = new Vec3(-1, 0, 0);
	public static final Vec3 NEGATIVE_Y_AXIS = new Vec3(0, -1, 0);
	public static final Vec3 NEGATIVE_Z_AXIS = new Vec3(0, 0, -1);

	public float x = 0;
	public float y = 0;
	public float z = 0;

	public Vec3() {}

	public Vec3(final double x, final double y, final double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	public Vec3(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(final Vec3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vec3(final float[] data) {
		x = data[0];
		y = data[1];
		z = data[2];
	}

	public Vec3(final double[] data) {
		x = (float) data[0];
		y = (float) data[1];
		z = (float) data[2];
	}

	public Vec3(final float[] data, final boolean flip) {
		if (flip) {
			z = data[0];
			y = data[1];
			x = data[2];
		} else {
			x = data[0];
			y = data[1];
			z = data[2];
		}
	}

	public static Vec3 centerOfGroup(final Collection<? extends Vec3> group) {
		final Vec3 center = new Vec3(0, 0, 0);

		for (final Vec3 v : group) {
			center.add(v);
		}

		if (group.size() != 0) {
			center.scale(1.0f / group.size());
		}

		return center;
	}

	public static Vec3 valueOf(String s) throws NumberFormatException {
		return parseVec3(s);
	}

	public static Vec3 parseVec3(String s) throws NumberFormatException {
		String unbracketed = s.replaceAll("[\\[\\](){}]", "");
		String[] numbers = unbracketed.split(",");
		float num0 = Float.parseFloat(numbers[0].strip());
		float num1 = Float.parseFloat(numbers[1].strip());
		float num2 = Float.parseFloat(numbers[2].strip());
		return new Vec3(num0, num1, num2);
	}

	public static Vec3 getSum(final Vec3 a, final Vec3 b) {
		return new Vec3(a).add(b);
	}

	public static Vec3 getDiff(final Vec3 a, final Vec3 b) {
		return new Vec3(a).sub(b);
	}

	public static Vec3 getProd(final Vec3 a, final Vec3 b) {
		return new Vec3(a).multiply(b);
	}

	public static Vec3 getQuotient(final Vec3 a, final Vec3 b) {
		return new Vec3(a).divide(b);
	}

	public static Vec3 getTransformed(final Vec3 a, Mat4 mat4) {
		return new Vec3(a).transform(mat4);
	}

	public static Vec3 getScaled(Vec3 a, float factor) {
		return new Vec3(a).scale(factor);
	}

	public static Vec3 getNormalized(final Vec3 a) {
		return new Vec3(a).normalize();
	}

	public static Vec3 getCross(final Vec3 a, final Vec3 b) {
		return new Vec3(a).cross(b);
	}

	public static Vec3 getLerped(final Vec3 from, final Vec3 toward, final float t) {
		return new Vec3(from).lerp(toward, t);
	}

	public static Vec3 getHermite(final Vec3 from, final Vec3 outTan, final Vec3 inTan, final Vec3 toward, final float t) {
		return new Vec3(from).hermite(outTan, inTan, toward, t);
	}

	public static Vec3 getBezier(final Vec3 from, final Vec3 outTan, final Vec3 inTan, final Vec3 toward, final float t) {
		return new Vec3(from).bezier(outTan, inTan, toward, t);
	}
	public Vec2 getProjected(Vec3 axis1, Vec3 axis2) {
		return new Vec2(dot(axis1), dot(axis2));
	}

	public boolean equalLocs(final Vec3 v) {
		if (v == null) return false;
		return (x == v.x) && (y == v.y) && (z == v.z);
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + " }";
	}

	public String toStringLessSpace() {
		return "{" + x + ", " + y + ", " + z + "}";
	}

	public float distance(final Vec3 a) {
		return distance(a.x, a.y, a.z);
//		return getDiff(this, a).length();
	}

	public float distance(final float ax, final float ay, final float az) {
		final float dx = ax - x;
		final float dy = ay - y;
		final float dz = az - z;

		return (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}

	public float distance(final Vec4 other) {
		return distance(other.getVec3());
	}

	public Vec3 rotate(Vec3 center, Quat quat) {
		sub(center);
		transform(quat);
		add(center);
		return this;
	}


	public Vec3 transform(Quat quat) {
		float uvx = quat.y * z - quat.z * y;
		float uvy = quat.z * x - quat.x * z;
		float uvz = quat.x * y - quat.y * x;
		float uuvx = quat.y * uvz - quat.z * uvy;
		float uuvy = quat.z * uvx - quat.x * uvz;
		float uuvz = quat.x * uvy - quat.y * uvx;
		float w2 = quat.w * 2;

		float newX = x + (uvx * w2) + (uuvx * 2);
		float newY = y + (uvy * w2) + (uuvy * 2);
		float newZ = z + (uvz * w2) + (uuvz * 2);

		return set(newX, newY, newZ);
	}

	public float[] toArray() {
		return new float[] {x, y, z};
	}
	public float[] toArray(float[] array) {
		if(array == null || array.length<3){
			return new float[] {x, y, z};
		}
		array[0] = x;
		array[1] = y;
		array[2] = z;
		return array;
	}

	public float[] toFloatArray() {
		return new float[] {x, y, z};
	}

	public double[] toDoubleArray() {
		return new double[] {x, y, z};
	}

	public Float[] toFloatArray2() {
		return new Float[] {x, y, z};
	}

	public short[] toShortArray() {
		return new short[] {(short) x, (short) y, (short) z};
	}

	public Vec3 translate(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public long[] toLongArray() {
		return new long[] {(long) x, (long) y, (long) z};
	}

	public Vec3 add(final Vec3 a) {
		x = x + a.x;
		y = y + a.y;
		z = z + a.z;
		return this;
	}

	public float dot(final Vec3 a) {
		return (x * a.x) + (y * a.y) + (z * a.z);
	}

	public float dot(final Vec4 a) {
		return (x * a.x) + (y * a.y) + (z * a.z);
	}

	public float dotNorm(final Vec3 a) {
		float len = length() * a.length();

		if (len != 0) {
			len = 1.0f / len;
		}

		return ((x * a.x) + (y * a.y) + (z * a.z))*len;
	}

	public Vec3 multiply(final Vec3 a) {
		x = x * a.x;
		y = y * a.y;
		z = z * a.z;
		return this;
	}

	public Vec3 divide(final Vec3 a) {
		x = x / a.x;
		y = y / a.y;
		z = z / a.z;
		return this;
	}

	public Vec3 sub(final Vec3 a) {
		x = x - a.x;
		y = y - a.y;
		z = z - a.z;
		return this;
	}

	public Vec3 addScaled(Vec3 vec, float scale) {
		x += scale * vec.x;
		y += scale * vec.y;
		z += scale * vec.z;
		return this;
	}

	public Vec3 negate() {
		return scale(-1f);
	}

	public Vec3 transform(final Mat4 mat4) {
		return transform(1, mat4);
	}

	public Vec3 transform(float w, final Mat4 mat4) {
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + (mat4.m20 * z) + w * mat4.m30;
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + (mat4.m21 * z) + w * mat4.m31;
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + (mat4.m22 * z) + w * mat4.m32;
		return set(newX, newY, newZ);
	}


	public Vec3 transform(final Mat4 mat4, float w, boolean fromHomo) {
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + (mat4.m20 * z) + w * mat4.m30;
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + (mat4.m21 * z) + w * mat4.m31;
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + (mat4.m22 * z) + w * mat4.m32;
		if(fromHomo && w != 0f){
			float newW = (x * mat4.m03) + (y * mat4.m13) + (z * mat4.m23) + w * mat4.m33;
			newX /= newW;
			newY /= newW;
			newZ /= newW;
		}
		return set(newX, newY, newZ);
	}


	public Vec3 transformInverted(Mat4 mat4) {
		return transformInverted(mat4, 0, false);
	}

	public Vec3 transformInverted(Mat4 mat4, float w, boolean fromHomo) {
		//The "inverted" part of this is wrong!
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
		if(fromHomo){
			float newW = (tmp03 * x) + (tmp13 * y) + (tmp23 * z) + (tmp33 * w);
			newX /= newW;
			newY /= newW;
			newZ /= newW;
		}


		return set(newX, newY, newZ);
	}

	public double degAngleTo(Vec3 a) {
		return (radAngleTo(a) * 180.0 / Math.PI);
	}

	public double radAngleTo(Vec3 a) {
		double cos = dot(a) / (length() * a.length());
		if (1 < cos) {
			cos = cos % (1.0);
			return (Math.acos(1 - cos));
		} else if (cos < -1) {
			cos = cos % (1.0);
			return (Math.acos(cos) + 2.0 * Math.PI); //ToDo check if stuff behaves correct when this is made positive by rotating 360 degrees
		}
		return Math.acos(cos);
	}

	public double radAngleTo2(Vec3 a) {
		double cos = dot(a) / (length() * a.length());
		if (1 < cos) {
			cos = 1 - cos;
		} else if (cos < -1) {
			return (Math.acos(cos % (1.0)) + 2 * Math.PI); //ToDo check if stuff behaves correct when this is made positive by rotating 360 degrees
		}
		return Math.copySign(Math.acos(cos), y);
	}

	public double getZrotToYaxis() {
		// not casting to float causes y=-1.0 to yield NaN because of rounding error
		float cos = y / ((float)Math.sqrt(x*x+y*y));
		if(x<0){
			return -Math.acos(cos);
		} else {

			return Math.acos(cos);
		}
	}

	public double getZrotToXaxis() {
		// not casting to float causes x=-1.0 to yield NaN because of rounding error
		float cos = x / ((float)Math.sqrt(x*x+y*y));

		if(0<=y){
			return -Math.acos(cos);
		} else {
			return Math.acos(cos);
		}
	}
	public double getAngleToZaxis() {
		double cos = z / length();
		return Math.acos(cos) - Math.PI / 2.0;
	}

	public Quat getQuatTo(Vec3 other) {
		Vec3 aNorm = new Vec3(this).normalize();
		Vec3 bNorm = new Vec3(other).normalize();

		Vec3 cross = new Vec3(aNorm).cross(bNorm).normalize();

		return new Quat().setFromAxisAngle(cross, (float) aNorm.radAngleTo(bNorm)).normalize().invertRotation();
	}

	public Vec3 scale(final double centerX, final double centerY, final double centerZ,
	                  final double scaleX, final double scaleY, final double scaleZ) {
		final float dx = this.x - (float) centerX;
		final float dy = this.y - (float) centerY;
		final float dz = this.z - (float) centerZ;
		this.x = (float) centerX + (dx * (float) scaleX);
		this.y = (float) centerY + (dy * (float) scaleY);
		this.z = (float) centerZ + (dz * (float) scaleZ);
		return this;
	}

	public Vec3 scale(final Vec3 center, final Vec3 scale) {
		sub(center).multiply(scale).add(center);
		return this;
	}
	public Vec3 scale(final Vec3 center, float scale) {
		sub(center).scale(scale).add(center);
		return this;
	}

	public Vec3 scale(final float factor) {
		x = x * factor;
		y = y * factor;
		z = z * factor;
		return this;
	}

	public Vec3 minimize(Vec3 a) {
		x = Math.min(x, a.x);
		y = Math.min(y, a.y);
		z = Math.min(z, a.z);
		return this;
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public Vec3 maximize(Vec3 a) {
		x = Math.max(x, a.x);
		y = Math.max(y, a.y);
		z = Math.max(z, a.z);
		return this;
	}

	public Vec3 normalize() {
		float len = length();

		if (len != 0) {
			len = 1.0f / len;
		}
		return scale(len);
	}

	public Vec3 cross(final Vec3 a) {
		float newX = (y * a.z) - (a.y * z);
		float newY = (a.x * z) - (x * a.z);
		float newZ = (x * a.y) - (a.x * y);

		set(newX, newY, newZ);
		return this;
	}

	public Vec3 crossNorm(final Vec3 a) {
		float len = length();
		float lenA = a.length();
		float scaleF = (lenA * len);

		if (scaleF == 0) {
			scaleF = 1;
		}
		float newX = ((y * a.z) - (a.y * z)) / scaleF;
		float newY = ((a.x * z) - (x * a.z)) / scaleF;
		float newZ = ((x * a.y) - (a.x * y)) / scaleF;

		set(newX, newY, newZ);
		return this;
	}

	public boolean isValid() {
		return !(Float.isNaN(this.x)
				|| Float.isNaN(this.y)
				|| Float.isNaN(this.z)
				|| Float.isInfinite(this.x)
				|| Float.isInfinite(this.y)
				|| Float.isInfinite(this.z));
	}

	public Vec3 lerp(final Vec3 a, final float t) {
		x = MathUtils.lerp(x, a.x, t);
		y = MathUtils.lerp(y, a.y, t);
		z = MathUtils.lerp(z, a.z, t);

		return this;
	}

	public Vec3 hermite(final Vec3 outTan, final Vec3 inTan, final Vec3 toward, final float t) {
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

	public Vec3 bezier(final Vec3 outTan, final Vec3 inTan, final Vec3 toward, final float t) {
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
		return this;
	}

	public Vec3 wikiToEuler(Quat q){
		// roll (x-axis rotation)
		double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
		double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
		double roll = Math.atan2(sinr_cosp, cosr_cosp);

		// pitch (y-axis rotation)
		double sinp = 2 * (q.w * q.y - q.z * q.x);
		// use 90 degrees if out of range
		double pitch = Math.abs(sinp) >= 1 ? Math.copySign(Math.PI / 2, sinp) : Math.asin(sinp);


		// yaw (z-axis rotation)
		double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
		double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
		double yaw = Math.atan2(siny_cosp, cosy_cosp);

		return set(roll, pitch, yaw);
	}


	public Vec3 clamp(Float min, Float max){
		float minf = min == null ? -Float.MAX_VALUE : min;
		float maxf = max == null ? -Float.MAX_VALUE : max;
		x = Math.max(minf, Math.min(maxf, x));
		y = Math.max(minf, Math.min(maxf, y));
		z = Math.max(minf, Math.min(maxf, z));
		return this;
	}
	public Vec4 getVec4() {
		return new Vec4(x, y, z, 0);
	}

	public Vec4 getVec4(final float w) {
		return new Vec4(x, y, z, w);
	}

	public Vec3 set(final Vec3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}
	public Vec3 set(final Vec2 v, float z) {
		x = v.x;
		y = v.y;
		this.z = z;
		return this;
	}

	public Vec3 set(final Vec4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public Vec3 set(final double vx, final double vy, final double vz) {
		x = (float) vx;
		y = (float) vy;
		z = (float) vz;
		return this;
	}

	public Vec3 set(final float vx, final float vy, final float vz) {
		x = vx;
		y = vy;
		z = vz;
		return this;
	}

	public Vec3 set(final float[] data) {
		x = data[0];
		y = data[1];
		z = data[2];
		return this;
	}

	public Color asIntColor() {
		float max = Math.max(x, Math.max(y, z));
		float scale = max <= 255 ? 1 : 255 / max;
		int red = (int) (x * scale);
		int green = (int) (y * scale);
		int blue = (int) (z * scale);
		return new Color(red, green, blue);
	}
	public Color asFloatColor() {
		return new Color(x, y, z);
	}
	public Vec3 setFromColor(Color color) {
		return this.set(color.getColorComponents(null));
	}

	public Vec3 setAsAxis(Quat quat){
		float angle = (float) Math.acos(MathUtils.clamp(quat.w, -1f, 1f)) * 2;
		float sinOfHalfAngle = (float) Math.sin(angle / 2.0);
		if(sinOfHalfAngle != 0) {
			float ax = quat.x / sinOfHalfAngle;
			float ay = quat.y / sinOfHalfAngle;
			float az = quat.z / sinOfHalfAngle;
			return set(ax, ay, az);
		}
		return set(0, 0, 1);
	}
	public int getPositionHash() {
		int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	public Vec3 setAsLocationFromMat(Mat4 mat4, Vec3 pivot) {
		x = mat4.m30 + ((mat4.m00 * pivot.x) + (mat4.m10 * pivot.y) + (mat4.m20 * pivot.z)) - pivot.x;
		y = mat4.m31 + ((mat4.m01 * pivot.x) + (mat4.m11 * pivot.y) + (mat4.m21 * pivot.z)) - pivot.y;
		z = mat4.m32 + ((mat4.m02 * pivot.x) + (mat4.m12 * pivot.y) + (mat4.m22 * pivot.z)) - pivot.z;

		return this;
	}

	public String toRoundedString(){
		int roundX = Math.round(x);
		int roundY = Math.round(y);
		int roundZ = Math.round(z);
		return "{ "
				+ (0 < roundX ? " " : "") + roundX + ", "
				+ (0 < roundY ? " " : "") + roundY + ", "
				+ (0 < roundZ ? " " : "") + roundZ + " }";
	}
}
