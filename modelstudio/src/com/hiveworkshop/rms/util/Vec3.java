package com.hiveworkshop.rms.util;

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

	public float getCoord(final byte dim) {
		switch (dim) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		}
		return 0;
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
			}
		}
	}

	public void translateCoord(final byte dim, final double value) {
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
		}
	}

	public void set(final Vec3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(final Vec4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(final double vx, final double vy, final double vz) {
		x = (float) vx;
		y = (float) vy;
		z = (float) vz;
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

	public double[] toArray() {
		return new double[] { x, y, z };
	}

	public float[] toFloatArray() {
		return new float[] {x, y, z};
	}

	public short[] toShortArray() {
		return new short[] { (short)x, (short)y, (short)z };
	}

	public long[] toLongArray() {
		return new long[] { (long)x, (long)y, (long)z };
	}

	public static Vec3 centerOfGroup(final Collection<? extends Vec3> group) {
		final Vec3 center = new Vec3();

		for (final Vec3 v : group) {
			center.add(v);
		}

		center.scale(1 / group.size());

		return center;
	}

	public float distance (final float ax, final float ay, final float az) {
		final float dx = ax - x;
		final float dy = ay - y;
		final float dz = az - z;

		return (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));

	}
	public float distance(final Vec3 other) {
		return distance(other.x, other.y, other.z);
	}

	public float distance(final Vec4 other) {
		return distance(other.x, other.y, other.z);
	}

	public Vec3 add(final Vec3 a, final Vec3 out) {
		out.x = x + a.x;
		out.y = y + a.y;
		out.z = z + a.z;

		return out;
	}

	public Vec3 add(final Vec3 a) {
		return add(a, this);
	}

	public void translate(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public float dot(final Vec3 a) {
		return (x * a.x) + (y * a.y) + (z * a.z);
	}

	public void scale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		final float dx = this.x - (float)centerX;
		final float dy = this.y - (float)centerY;
		final float dz = this.z - (float)centerZ;
		this.x = (float)centerX + (dx * (float)scaleX);
		this.y = (float)centerY + (dy * (float)scaleY);
		this.z = (float)centerZ + (dz * (float)scaleZ);
	}

	public Vec3 scale(final float factor) {
		return scale(factor, this);
	}

	public Vec3 scale(final float factor, final Vec3 out) {
		out.x = x * factor;
		out.y = y * factor;
		out.z = z * factor;
		
		return out;
	}

	public void rotate(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		rotateVertex(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, this);
	}

	public static void rotateVertex(final Vec3 center, final Vec3 axis, final double radians, final Vec3 vertex) {
		final double centerX = center.x;
		final double centerY = center.y;
		final double centerZ = center.z;
		final double vertexX = vertex.x;
		final double vertexY = vertex.y;
		final double vertexZ = vertex.z;
		final double deltaX = vertexX - centerX;
		final double deltaY = vertexY - centerY;
		final double deltaZ = vertexZ - centerZ;
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
			vertex.x = (float)centerX - (float)deltaX;
			vertex.y = (float)centerY - (float)deltaY;
			vertex.z = (float)centerY - (float)deltaZ;
		}
		final double resultDeltaX = vertexX * cosRadians;
		throw new UnsupportedOperationException("NYI");
	}

	public static void rotateVertex(final double centerX, final double centerY, final double centerZ,
			final double radians, final byte firstXYZ, final byte secondXYZ, final Vec3 vertex) {
		final double x1 = vertex.getCoord(firstXYZ);
		final double y1 = vertex.getCoord(secondXYZ);
		final double cx;// = coordinateSystem.geomX(centerX);
		switch (firstXYZ) {
		case 0:
			cx = centerX;
			break;
		case 1:
			cx = centerY;
			break;
		default:
		case 2:
			cx = centerZ;
			break;
		}
		final double dx = x1 - cx;
		final double cy;// = coordinateSystem.geomY(centerY);
		switch (secondXYZ) {
		case 0:
			cy = centerX;
			break;
		case 1:
			cy = centerY;
			break;
		default:
		case 2:
			cy = centerZ;
			break;
		}
		final double dy = y1 - cy;
		final double r = Math.sqrt((dx * dx) + (dy * dy));
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double nextDim = (Math.cos(verAng + radians) * r) + cx;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(firstXYZ, nextDim);
		}
		// if( getDimEditable(dim2) )
		nextDim = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(secondXYZ, nextDim);
		}
	}

	public Vec3 normalize(final Vec3 out) {
		float len = lengthSquared();

		if (len != 0) {
			len = 1 / len;
		}

		out.x = x * len;
		out.y = y * len;
		out.z = z * len;

		return out;
	}

	public Vec3 normalize() {
		return normalize(this);
	}

	public Vec3 cross(final Vec3 a, final Vec3 out) {
		final float ax = a.x;
		final float ay = a.y;
		final float az = a.z;

		out.x = (y * az) - (ay * z);
		out.y = (ax * z) - (x * az);
		out.z = (x * ay) - (ax * y);

		return out;
	}

	public Vec3 cross(final Vec3 a) {
		return cross(a, this);
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public Vec3 sub(final Vec3 a, final Vec3 out) {
		out.x = x - a.x;
		out.y = y - a.y;
		out.z = z - a.z;

		return out;
	}

	public Vec3 sub(final Vec3 a) {
		return sub(a, this);
	}

	public Vec3 negate(final Vec3 out) {
		out.x = -x;
		out.y = -y;
		out.z = -z;

		return out;
	}

	public Vec3 negate() {
		return negate(this);
	}

	public Vec3 lerp(final Vec3 a, final float t, final Vec3 out) {
		out.x = MathUtils.lerp(x, a.x, t);
		out.y = MathUtils.lerp(y, a.y, t);
		out.z = MathUtils.lerp(z, a.z, t);

		return out;
	}

	public Vec3 lerp(final Vec3 a, final float t) {
		return lerp(a, t, this);
	}

	public Vec3 hermite(final Vec3 outTan, final Vec3 inTan, final Vec3 a, final float t, final Vec3 out) {
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

	public Vec3 hermite(final Vec3 outTan, final Vec3 inTan, final Vec3 a, final float t) {
		return hermite(outTan, inTan, a, t, this);
	}

	public Vec3 bezier(final Vec3 outTan, final Vec3 inTan, final Vec3 a, final float t, final Vec3 out) {
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

		return out;
	}

	public Vec3 bezier(final Vec3 outTan, final Vec3 inTan, final Vec3 a, final float t) {
		return bezier(outTan, inTan, a, t, this);
	}
}
