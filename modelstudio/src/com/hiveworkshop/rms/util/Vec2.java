package com.hiveworkshop.rms.util;

import java.util.Collection;

public class Vec2 {
	public static final Vec2 ORIGIN = new Vec2();
	public static final Vec2 ONE = new Vec2(1,1);
	public static final Vec2 X_AXIS = new Vec2(1,0);
	public static final Vec2 Y_AXIS = new Vec2(0,1);

	public float x = 0;
	public float y = 0;

	public Vec2() {
	}

	public Vec2(final double x, final double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public Vec2(final Vec2 old) {
		this.x = old.x;
		this.y = old.y;
	}
	public Vec2(final float[] floats) {
		this.x = floats[0];
		this.y = floats[1];
	}

	public static Vec2 centerOfGroup(final Collection<? extends Vec2> group) {
		final Vec2 center = new Vec2();

		for (final Vec2 v : group) {
			center.add(v);
		}

		center.scale(1.0f / group.size());

		return center;
	}

	public float getCoord(final int dim) {
		return switch (dim) {
			case 0 -> x;
			case 1 -> y;
			default -> 0;
		};
	}

	public Vec2 setCoord(final int dim, final double value) {
		if (!Double.isNaN(value)) {
			switch (dim) {
				case 0 -> x = (float) value;
				case 1 -> y = (float) value;
			}
		}
		return this;
	}

	public static Vec2 getProd(Vec2 a, Vec2 b) {
		return new Vec2(a).mul(b);
	}

	public static Vec2 getScaled(Vec2 a, float factor) {
		return new Vec2(a).scale(factor);
	}

	public Vec2 getScaled(float factor) {
		return new Vec2(this).scale(factor);
	}

	public static Vec2 getSum(Vec2 a, Vec2 b) {
		return new Vec2(a).add(b);
	}

	public static Vec2 getDif(Vec2 a, Vec2 b) {
		return new Vec2(a).sub(b);
	}

	public Vec2 translateCoord(final byte dim, final double value) {
		switch (dim) {
			case 0 -> x += value;
			case 1 -> y += value;
		}
		return this;
	}

	public Vec2 setProjection(Vec3 vec3, byte dim1, byte dim2) {
		this.x += vec3.getCoord(dim1);
		this.y += vec3.getCoord(dim2);
		return this;
	}
//	public void rotate(final double centerX, final double centerY, final double radians, final byte firstXYZ,
//	                   final byte secondXYZ) {
//		rotateVertex(centerX, centerY, radians, firstXYZ, secondXYZ, this);
//	}

	public Vec2 translate(final double x, final double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + " }";
	}

	public float distance(final Vec2 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;

		return (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	public Vec2 scale(final double centerX, final double centerY, final double scaleX, final double scaleY) {
		final float dx = this.x - (float) centerX;
		final float dy = this.y - (float) centerY;
		this.x = (float) centerX + (dx * (float) scaleX);
		this.y = (float) centerY + (dy * (float) scaleY);
		return this;
	}

	public Vec2 scale(Vec2 center, Vec2 a) {
		this.sub(center).mul(a).add(center);
//		final float dx = this.x - center.x;
//		final float dy = this.y - center.y;
//		this.x = center.x + (dx * a.x);
//		this.y = center.y + (dy * a.y);
		return this;
	}

	public Vec2 rotate(final double centerX, final double centerY, final double radians,
	                   final byte firstXYZ, final byte secondXYZ) {
		final double x1 = getCoord(firstXYZ);
		final double y1 = getCoord(secondXYZ);
		final double cx = switch (firstXYZ) {
			case 0 -> centerX;
			case 1 -> centerY;
			case 2 -> 0;
			default -> 0;
		};// = coordinateSystem.geomX(centerX);
		final double dx = x1 - cx;
		final double cy = switch (secondXYZ) {
			case 0 -> centerX;
			case 1 -> centerY;
			case 2 -> 0;
			default -> 0;
		};// = coordinateSystem.geomY(centerY);
		final double dy = y1 - cy;
		final double r = Math.sqrt((dx * dx) + (dy * dy));
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double newFirstCoord = (Math.cos(verAng + radians) * r) + cx;
		if (!Double.isNaN(newFirstCoord)) {
			setCoord(firstXYZ, (float) newFirstCoord);
		}
		// if( getDimEditable(dim2) )
		double newSecondCoord = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(newSecondCoord)) {
			setCoord(secondXYZ, (float) newSecondCoord);
		}
		return this;
	}

	public Vec2 rotate(Vec2 center, Quat quat) {
		sub(center);
		transform(quat);
		add(center);
		return this;
	}

	public float lengthSquared() {
		return (x * x) + (y * y);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float[] toFloatArray() {
		return new float[] {x, y};
	}

	public float[] toArray() {
		return new float[] {x, y};
	}

	public Vec2 normalize() {
		float len = lengthSquared();

		if (len != 0) {
			len = 1 / len;
		}

		x = x * len;
		y = y * len;
		return this;
	}

	public Vec2 scale(final float factor) {
		x = x * factor;
		y = y * factor;
		return this;
	}

	public Vec2 mul(final Vec2 a) {
		x = x * a.x;
		y = y * a.y;
		return this;
	}

	public Vec2 div(final Vec2 a) {
		x = x / a.x;
		y = y / a.y;
		return this;
	}

	public Vec2 add(final Vec2 a) {
		x = x + a.x;
		y = y + a.y;

		return this;
	}

	public Vec2 sub(final Vec2 a) {
		x = x - a.x;
		y = y - a.y;

		return this;
	}

	public Vec2 minimize(Vec2 a) {
		x = Math.min(x, a.x);
		y = Math.min(y, a.y);
		return this;
	}

	public Vec2 maximize(Vec2 a) {
		x = Math.max(x, a.x);
		y = Math.max(y, a.y);
		return this;
	}

	public Vec2 lerp(final Vec2 a, final float t) {
		x = MathUtils.lerp(x, a.x, t);
		y = MathUtils.lerp(y, a.y, t);
		return this;
	}

	public Vec2 hermite(final Vec2 outTan, final Vec2 inTan, final Vec2 toward, final float t) {
		final float factorTimes2 = t * t;
		final float factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final float factor2 = (factorTimes2 * (t - 2)) + t;
		final float factor3 = factorTimes2 * (t - 1);
		final float factor4 = factorTimes2 * (3 - (2 * t));

		x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (toward.x * factor4);
		y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (toward.y * factor4);
		return this;
	}

	public Vec2 bezier(final Vec2 outTan, final Vec2 inTan, final Vec2 toward, final float t) {
		final float invt = 1 - t;
		final float factorSquared = t * t;
		final float inverseFactorSquared = invt * invt;
		final float factor1 = inverseFactorSquared * invt;
		final float factor2 = 3 * t * inverseFactorSquared;
		final float factor3 = 3 * factorSquared * invt;
		final float factor4 = factorSquared * t;

		x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (toward.x * factor4);
		y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (toward.y * factor4);
		return this;
	}

	public Vec2 set(final Vec2 v) {
		x = v.x;
		y = v.y;
		return this;
	}

	public Vec2 set(final float x, final float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vec2 set(final double x, final double y) {
		this.x = (float) x;
		this.y = (float) y;
		return this;
	}

	public Vec2 transform(Quat quat) {
		float uvx = - quat.z * y;
		float uvy = quat.z * x;
		float uvz = quat.x * y - quat.y * x;
		float uuvx = quat.y * uvz - quat.z * uvy;
		float uuvy = quat.z * uvx - quat.x * uvz;
		float w2 = quat.w * 2;

		float newX = x + (uvx * w2) + (uuvx * 2);
		float newY = y + (uvy * w2) + (uuvy * 2);

		return set(newX, newY);
	}

	public Vec2 transform(final Mat4 mat4) {
		// This one works for the UV-editor, which might do something wrong...
//		float newX = (mat4.m00 * x) + (mat4.m10 * y) + mat4.m30;
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + mat4.m31;
		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + mat4.m32;
//		return set(newX, newY, newZ);
		return set(newY, newZ);
	}

	public Vec2 transform2(final Mat4 mat4) {
		// This works for TVertAnims
		float newX = (mat4.m00 * x) + (mat4.m10 * y) + mat4.m30;
		float newY = (mat4.m01 * x) + (mat4.m11 * y) + mat4.m31;
//		float newZ = (mat4.m02 * x) + (mat4.m12 * y) + mat4.m32;
		return set(newX, newY);
	}

	public Vec2 setAsProjection(Vec3 vec3, Mat4 mat4){
		float l_w = 1f / ((vec3.x * mat4.m03) + (vec3.y * mat4.m13) + (vec3.z * mat4.m23) + mat4.m33);
		float newX = (mat4.m00 * vec3.x) + (mat4.m10 * vec3.y) + (mat4.m20 * vec3.z) + mat4.m30;
		float newY = (mat4.m01 * vec3.x) + (mat4.m11 * vec3.y) + (mat4.m21 * vec3.z) + mat4.m31;
		return set(newX, newY).scale(l_w);
	}

}
