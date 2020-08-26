package com.hiveworkshop.rms.util;

import java.util.Collection;

public class Vector2 {
	public static final Vector2 ORIGIN = new Vector2();

	public float x = 0;
	public float y = 0;

	public Vector2() {

	}

	public Vector2(final double x, final double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public Vector2(final Vector2 old) {
		this.x = old.x;
		this.y = old.y;
	}

	public double getCoord(final float dim) {
		final int i = (int) dim;
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
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
		}
	}

	public void set(final Vector2 v) {
		x = v.x;
		y = v.y;
	}

	public void translate(final double x, final double y) {
		this.x += x;
		this.y += y;
	}

	public void scale(final double centerX, final double centerY, final double scaleX, final double scaleY) {
		final float dx = this.x - (float)centerX;
		final float dy = this.y - (float)centerY;
		this.x = (float)centerX + (dx * (float)scaleX);
		this.y = (float)centerY + (dy * (float)scaleY);
	}

	public void rotate(final double centerX, final double centerY, final double radians, final byte firstXYZ,
			final byte secondXYZ) {
		rotateVertex(centerX, centerY, radians, firstXYZ, secondXYZ, this);
	}

	public static void rotateVertex(final double centerX, final double centerY, final double radians,
			final byte firstXYZ, final byte secondXYZ, final Vector2 vertex) {
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
			cx = 0;
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
			cy = 0;
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
			vertex.setCoord(secondXYZ, (Math.sin(verAng + radians) * r) + cy);
		}
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + " }";
	}

	public static Vector2 centerOfGroup(final Collection<? extends Vector2> group) {
		final Vector2 center = new Vector2();

		for (final Vector2 v : group) {
			center.add(v);
		}

		center.scale(1 / group.size());

		return center;
	}

	public float distance(final Vector2 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;

		return (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	public Vector2 normalize(final Vector2 out) {
		float len = lengthSquared();

		if (len != 0) {
			len = 1 / len;
		}

		out.x = x * len;
		out.y = y * len;

		return out;
	}

	public Vector2 normalize() {
		return normalize(this);
	}

	public float lengthSquared() {
		return (x * x) + (y * y);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public Vector2 scale(final float factor, final Vector2 out) {
		out.x = x * factor;
		out.y = y * factor;

		return out;
	}

	public Vector2 scale(final float factor) {
		return scale(factor, this);
	}

	public Vector2 add(final Vector2 a, final Vector2 out) {
		out.x = x + a.x;
		out.y = y + a.y;

		return out;
	}

	public Vector2 add(final Vector2 a) {
		return add(a, this);
	}

	public Vector2 sub(final Vector2 a, final Vector2 out) {
		out.x = x - a.x;
		out.y = y - a.y;

		return out;
	}

	public Vector2 sub(final Vector2 a) {
		return sub(a, this);
	}
}
