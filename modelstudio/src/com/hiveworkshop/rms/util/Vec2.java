package com.hiveworkshop.rms.util;

import java.util.Collection;

public class Vec2 {
	public static final Vec2 ORIGIN = new Vec2();

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

	public float getCoord(final int dim) {
		switch (dim) {
		case 0:
			return x;
		case 1:
			return y;
		}
		return 0;
	}

	public void setCoord(final int dim, final double value) {
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

	public void set(final Vec2 v) {
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
			final byte firstXYZ, final byte secondXYZ, final Vec2 vertex) {
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
			vertex.setCoord(firstXYZ, (float) nextDim);
		}
		// if( getDimEditable(dim2) )
		nextDim = (Math.sin(verAng + radians) * r) + cy;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(secondXYZ, (float) ((Math.sin(verAng + radians) * r) + cy));
		}
	}

	@Override
	public String toString() {
		return "{ " + x + ", " + y + " }";
	}

	public static Vec2 centerOfGroup(final Collection<? extends Vec2> group) {
		final Vec2 center = new Vec2();

		for (final Vec2 v : group) {
			center.add(v);
		}

		center.scale(1 / group.size());

		return center;
	}

	public float distance(final Vec2 a) {
		final float dx = a.x - x;
		final float dy = a.y - y;

		return (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	public Vec2 normalize(final Vec2 out) {
		float len = lengthSquared();

		if (len != 0) {
			len = 1 / len;
		}

		out.x = x * len;
		out.y = y * len;

		return out;
	}

	public Vec2 normalize() {
		return normalize(this);
	}

	public float lengthSquared() {
		return (x * x) + (y * y);
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public Vec2 scale(final float factor, final Vec2 out) {
		out.x = x * factor;
		out.y = y * factor;

		return out;
	}

	public Vec2 scale(final float factor) {
		return scale(factor, this);
	}

	public Vec2 add(final Vec2 a, final Vec2 out) {
		out.x = x + a.x;
		out.y = y + a.y;

		return out;
	}

	public Vec2 add(final Vec2 a) {
		return add(a, this);
	}

	public Vec2 sub(final Vec2 a, final Vec2 out) {
		out.x = x - a.x;
		out.y = y - a.y;

		return out;
	}

	public Vec2 sub(final Vec2 a) {
		return sub(a, this);
	}

	public Vec2 lerp(final Vec2 a, final float t, final Vec2 out) {
		out.x = MathUtils.lerp(x, a.x, t);
		out.y = MathUtils.lerp(y, a.y, t);

		return out;
	}

	public Vec2 lerp(final Vec2 a, final float t) {
		return lerp(a, t, this);
	}

	public Vec2 hermite(final Vec2 outTan, final Vec2 inTan, final Vec2 a, final float t, final Vec2 out) {
		final float factorTimes2 = t * t;
		final float factor1 = (factorTimes2 * ((2 * t) - 3)) + 1;
		final float factor2 = (factorTimes2 * (t - 2)) + t;
		final float factor3 = factorTimes2 * (t - 1);
		final float factor4 = factorTimes2 * (3 - (2 * t));
		
		out.x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (a.x * factor4);
		out.y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (a.y * factor4);

		return out;
	}

	public Vec2 hermite(final Vec2 outTan, final Vec2 inTan, final Vec2 a, final float t) {
		return hermite(outTan, inTan, a, t, this);
	}

	public Vec2 bezier(final Vec2 outTan, final Vec2 inTan, final Vec2 a, final float t, final Vec2 out) {
		final float invt = 1 - t;
		final float factorSquared = t * t;
		final float inverseFactorSquared = invt * invt;
		final float factor1 = inverseFactorSquared * invt;
		final float factor2 = 3 * t * inverseFactorSquared;
		final float factor3 = 3 * factorSquared * invt;
		final float factor4 = factorSquared * t;
		
		out.x = (x * factor1) + (outTan.x * factor2) + (inTan.x * factor3) + (a.x * factor4);
		out.y = (y * factor1) + (outTan.y * factor2) + (inTan.y * factor3) + (a.y * factor4);

		return out;
	}

	public Vec2 bezier(final Vec2 outTan, final Vec2 inTan, final Vec2 a, final float t) {
		return bezier(outTan, inTan, a, t, this);
	}
}
