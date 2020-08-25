package com.hiveworkshop.rms.util;

import java.util.Collection;

public class Vertex3 {
	public static final Vertex3 ORIGIN = new Vertex3(0, 0, 0);
	public float x = 0;
	public float y = 0;
	public float z = 0;

	public Vertex3() {

	}
	
	public Vertex3(final double x, final double y, final double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	public Vertex3(final Vertex3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vertex3(final float[] data) {
		x = data[0];
		y = data[1];
		z = data[2];
	}

	public Vertex3(final double[] data) {
		x = (float) data[0];
		y = (float) data[1];
		z = (float) data[2];
	}

	public Vertex3(final float[] data, final boolean flip) {
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

	public double getCoord(final byte dim) {
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

	public static float getCoord(final Vertex4 vector, final byte dim) {
		switch (dim) {
		case 0:
			return vector.x;
		case 1:
			return vector.y;
		case 2:
			return vector.z;
		default:
			throw new IllegalStateException();
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

	public void set(final Vertex3 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(final Vertex4 v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(final double vx, final double vy, final double vz) {
		x = (float) vx;
		y = (float) vy;
		z = (float) vz;
	}

	public boolean equalLocs(final Vertex3 v) {
		return (x == v.x) && (y == v.y) && (z == v.z);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
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
		return new float[] { (float) x, (float) y, (float) z };
	}

	public short[] toShortArray() {
		return new short[] { (short)x, (short)y, (short)z };
	}

	public long[] toLongArray() {
		return new long[] { (long)x, (long)y, (long)z };
	}

	public static Vertex3 centerOfGroup(final Collection<? extends Vertex3> group) {
		double xTot = 0;
		double yTot = 0;
		double zTot = 0;
		for (final Vertex3 v : group) {
			xTot += v.getX();
			yTot += v.getY();
			zTot += v.getZ();
		}
		xTot /= group.size();
		yTot /= group.size();
		zTot /= group.size();
		return new Vertex3(xTot, yTot, zTot);
	}

	public double distance (final float ax, final float ay, final float az) {
		final double dx = ax - x;
		final double dy = ay - y;
		final double dz = az - z;

		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));

	}
	public double distance(final Vertex3 other) {
		return distance(other.x, other.y, other.z);
	}

	public double distance(final Vertex4 other) {
		return distance(other.x, other.y, other.z);
	}

	public double vectorMagnitude() {
		return distance(ORIGIN);
	}

	public Vertex3 delta(final Vertex3 other) {
		return new Vertex3(other.x - x, other.y - y, other.z - z);
	}

	public Vertex3 subtract(final Vertex3 other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}

	public Vertex3 add(final Vertex3 other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public Vertex3 crossProduct(final Vertex3 other) {
		final double x2 = other.x;
		final double y2 = other.y;
		final double z2 = other.z;
		return crossProduct(x2, y2, z2);
	}

	private Vertex3 crossProduct(final double x2, final double y2, final double z2) {
		return new Vertex3((y * z2) - (y2 * z), (x2 * z) - (x * z2), (x * y2) - (x2 * y));
	}

	public void translate(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public double dotProduct(final Vertex3 other) {
		return (other.x * x) + (other.y * y) + (other.z * z);
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

	public Vertex3 scale(final double factor) {
		this.x *= factor;
		this.y *= factor;
		this.z *= factor;
		return this;
	}

	public void rotate(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		rotateVertex(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, this);
	}

	public static void rotateVertex(final Vertex3 center, final Vertex3 axis, final double radians, final Vertex3 vertex) {
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
			final double radians, final byte firstXYZ, final byte secondXYZ, final Vertex3 vertex) {
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
			vertex.setCoord(firstXYZ, (Math.cos(verAng + radians) * r) + cx);
		}
		// if( getDimEditable(dim2) )
		nextDim = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(secondXYZ, (Math.sin(verAng + radians) * r) + cy);
		}
	}

	public Vertex3 normalize() {
		final double magnitude = Math.sqrt((x * x) + (y * y) + (z * z));
		x /= magnitude;
		y /= magnitude;
		z /= magnitude;
		return this;
	}

	public Vertex3 cross(final Vertex3 other) {
		return cross(this, other, new Vertex3());
	}

	public static Vertex3 cross(final Vertex3 a, final Vertex3 b, final Vertex3 out) {
		final float ax = a.x;
		final float ay = a.y;
		final float az = a.z;
		final float bx = b.x;
		final float by = b.y;
		final float bz = b.z;

		out.x = (ay * bz) - (by * az);
		out.y = (bx * az) - (ax * bz);
		out.z = (ax * by) - (bx * ay);

		return out;
	}

	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public Vertex3 sub(final Vertex3 other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}

	public static Vertex3 sub(final Vertex3 a, final Vertex3 b, final Vertex3 out) {
		out.x = a.x - b.x;
		out.y = a.y - b.y;
		out.z = a.z - b.z;

		return out;
	}

	public void inverse() {
		x = -x;
		y = -y;
		z = -z;
	}
}
