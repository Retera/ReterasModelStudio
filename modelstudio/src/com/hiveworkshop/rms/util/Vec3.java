package com.hiveworkshop.rms.util;

import java.awt.*;
import java.util.Collection;

public class Vec3 {
	public static final Vec3 ORIGIN = new Vec3();

	public float x = 0;
	public float y = 0;
	public float z = 0;

	public Vec3() {

	}

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

	protected static double getCenterDimCoord(double centerX, double centerY, double centerZ, byte dim) {
		return switch (dim) {
			case 0 -> centerX;
			case 1 -> centerY;
			case -1 -> -centerX;
			case -2 -> -centerY;
			case -3 -> -centerZ;
			case 2 -> centerZ;
			default -> centerZ;
		};
	}

	protected static double getCenterDimCoord(Vec3 center, byte dim) {
		return switch (dim) {
			case 0 -> center.x;
			case 1 -> center.y;
			case -1 -> -center.x;
			case -2 -> -center.y;
			case -3 -> -center.z;
			case 2 -> center.z;
			default -> center.z;
		};
	}

	public static void rotateVertex(final Vec3 center, final Vec3 axis, final double radians, final Vec3 vertex) {
//        final double centerX = center.x;
//        final double centerY = center.y;
//        final double centerZ = center.z;
//        final double vertexX = vertex.x;
//        final double vertexY = vertex.y;
//        final double vertexZ = vertex.z;
//        final double deltaX = vertexX - centerX;
//        final double deltaY = vertexY - centerY;
//        final double deltaZ = vertexZ - centerZ;
//        double radiansToApply;
//        final double twoPi = Math.PI * 2;
//        if (radians > Math.PI) {
//            radiansToApply = (radians - twoPi) % twoPi;
//        } else if (radians <= -Math.PI) {
//            radiansToApply = (radians + twoPi) % twoPi;
//        } else {
//            radiansToApply = radians;
//        }
//        final double cosRadians = Math.cos(radiansToApply);
//        if (radiansToApply == Math.PI) {
//            vertex.x = (float) centerX - (float) deltaX;
//            vertex.y = (float) centerY - (float) deltaY;
//            vertex.z = (float) centerY - (float) deltaZ;
//        }
//        final double resultDeltaX = vertexX * cosRadians;
//        throw new UnsupportedOperationException("NYI");
	}

	public static void rotateVertex2(final Vec3 center, final Vec3 axis, final double radians, final Vec3 vertex) {
		Vec3 delta = getDiff(vertex, center);


		double radiansToApply;
		final double twoPi = Math.PI * 2;
		if (radians > Math.PI) {
			radiansToApply = (radians - twoPi) % twoPi;
		} else if (radians <= -Math.PI) {
			radiansToApply = (radians + twoPi) % twoPi;
		} else {
			radiansToApply = radians;
		}
		final double cosRadians = Math.cos(radiansToApply);
		if (radiansToApply == Math.PI) {
			vertex.x = (float) center.x - (float) delta.x;
			vertex.y = (float) center.y - (float) delta.y;
			vertex.z = (float) center.z - (float) delta.z;
		}
//        final double resultDeltaX = vertexX * cosRadians;
		throw new UnsupportedOperationException("NYI");
	}

	public static Vec3 centerOfGroup(final Collection<? extends Vec3> group) {
		final Vec3 center = new Vec3();

		for (final Vec3 v : group) {
			center.add(v);
		}

		center.scale(1.0f / group.size());

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
		return new Vec3(from).hermite(outTan, inTan, toward, t);
	}

	public float getCoord(final byte dim) {
		return switch (dim) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			case -1 -> -x;
			case -2 -> -y;
			case -3 -> -z;
			default -> 0;
		};
	}

	public Vec2 getProjected(byte dim1, byte dim2) {
		return new Vec2(getCoord(dim1), getCoord(dim2));
	}

	public boolean equalLocs(final Vec3 v) {
		return (x == v.x) && (y == v.y) && (z == v.z);
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + ", " + z + " }";
	}

	public String toStringLessSpace() {
		return "{" + x + ", " + y + ", " + z + "}";
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

	public Vec3 rotate(final double centerX, final double centerY, final double centerZ, final double radians,
	                   final byte firstXYZ, final byte secondXYZ) {
		final double x1 = getCoord(firstXYZ);
		final double y1 = getCoord(secondXYZ);
		final double cx = getCenterDimCoord(centerX, centerY, centerZ, firstXYZ);// = coordinateSystem.geomX(centerX);
		final double dx = x1 - cx;
		final double cy = getCenterDimCoord(centerX, centerY, centerZ, secondXYZ);// = coordinateSystem.geomY(centerY);
		final double dy = y1 - cy;
		final double r = Math.sqrt((dx * dx) + (dy * dy));
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double newFirstCoord = (Math.cos(verAng + radians) * r) + cx;
		if (!Double.isNaN(newFirstCoord)) {
			setCoord(firstXYZ, newFirstCoord);
		}
		// if( getDimEditable(dim2) )
		double newSecondCoord = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(newSecondCoord)) {
			setCoord(secondXYZ, newSecondCoord);
		}
		return this;
	}

	public Vec3 rotate(Vec3 center, final double radians,
	                   final byte firstXYZ, final byte secondXYZ) {
		final double x1 = getCoord(firstXYZ);
		final double y1 = getCoord(secondXYZ);
		final double cx = getCenterDimCoord(center, firstXYZ);
		final double dx = x1 - cx;
		final double cy = getCenterDimCoord(center, secondXYZ);
		final double dy = y1 - cy;
		final double r = Math.sqrt((dx * dx) + (dy * dy));
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		double newFirstCoord = (Math.cos(verAng + radians) * r) + cx;
		if (!Double.isNaN(newFirstCoord)) {
			setCoord(firstXYZ, newFirstCoord);
		}
		double newSecondCoord = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(newSecondCoord)) {
			setCoord(secondXYZ, newSecondCoord);
		}
		return this;
	}

	public float[] toArray() {
		return new float[] {x, y, z};
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

	public float distance(final Vec3 a) {
		return getDiff(this, a).length();
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

	public Vec3 negate() {
		return scale(-1f);
	}

	public Vec3 transform(final Vec3 a, final Mat4 mat4) {
		float newX = (mat4.m00 * a.x) + (mat4.m10 * a.y) + (mat4.m20 * a.z) + mat4.m30;
		float newY = (mat4.m01 * a.x) + (mat4.m11 * a.y) + (mat4.m21 * a.z) + mat4.m31;
		float newZ = (mat4.m02 * a.x) + (mat4.m12 * a.y) + (mat4.m22 * a.z) + mat4.m32;
		return new Vec3(newX, newY, newZ);
	}

	public Vec3 transform(final Mat4 mat4) {
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + (mat4.m20 * z) + mat4.m30;
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + (mat4.m21 * z) + mat4.m31;
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + (mat4.m22 * z) + mat4.m32;
		return set(newX, newY, newZ);
	}

	public double degAngleTo(Vec3 a) {

		return (radAngleTo(a) * 180 / Math.PI);
	}

	public double radAngleTo(Vec3 a) {
		float dot = dot(a);
		float length = length();
		float lengthA = a.length();
		double cos = dot / (length * lengthA);
		if (cos > 1) {
			cos = cos % (1.0);
			return (Math.acos(1 - cos));
		} else if (cos < -1) {
			cos = cos % (1.0);
			return (Math.acos(cos) + 2 * Math.PI); //ToDo check if stuff behaves correct when this is made positive by rotating 360 degrees
		}
		return Math.acos(cos);
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
		Vec3 delta = this.sub(center);
		Vec3 scaleRes = delta.multiply(scale);
		set(scaleRes.add(center));
		return this;
	}

	public Vec3 scaleCentered(final Vec3 center, final Vec3 scale) {
		return sub(center).multiply(scale).add(center);
	}

	public Vec3 scale(final float factor) {
		x = x * factor;
		y = y * factor;
		z = z * factor;
		return this;
	}

	public Vec3 setCoord(final byte dim, final double value) {
		if (!Double.isNaN(value)) {
			switch (dim) {
				case 0 -> x = (float) value;
				case 1 -> y = (float) value;
				case 2 -> z = (float) value;
				case -1 -> x = (float) -value;
				case -2 -> y = (float) -value;
				case -3 -> z = (float) -value;
//                case -32 -> z = (float) -value;
			}
		}
		return this;
	}

	public Vec3 setCoords(byte dim1, byte dim2, Vec2 vec2) {
		setCoord(dim1, vec2.x);
		setCoord(dim2, vec2.y);
		return this;
	}

	public Vec3 translateCoord(final byte dim, final double value) {
		switch (dim) {
			case 0 -> x += value;
			case 1 -> y += value;
			case 2 -> z += value;
			case -1 -> x -= value;
			case -2 -> y -= value;
			case 3 -> z -= value;
		}
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
			len = 1 / len;
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
}
