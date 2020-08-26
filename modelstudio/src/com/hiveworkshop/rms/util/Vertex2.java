package com.hiveworkshop.rms.util;

import java.util.Collection;

public class Vertex2 {
	public static final Vertex2 ORIGIN = new Vertex2(0, 0);
	public float x = 0;
	public float y = 0;

	public Vertex2(final double x, final double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public Vertex2(final Vertex2 old) {
		this.x = old.x;
		this.y = old.y;
	}

	public void setX(final double x) {
		this.x = (float) x;
	}

	public void setY(final double y) {
		this.y = (float) y;
	}

	public Vertex2 subtract(final Vertex2 other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
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

	public void setTo(final Vertex2 v) {
		x = v.x;
		y = v.y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
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
			final byte firstXYZ, final byte secondXYZ, final Vertex2 vertex) {
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
			vertex.setCoord(firstXYZ, (Math.cos(verAng + radians) * r) + cx);
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

	public static Vertex2 centerOfGroup(final Collection<? extends Vertex2> group) {
		double xTot = 0;
		double yTot = 0;
		for (final Vertex2 v : group) {
			xTot += v.getX();
			yTot += v.getY();
		}
		xTot /= group.size();
		yTot /= group.size();
		return new Vertex2(xTot, yTot);
	}

	public double distance(final Vertex2 other) {
		final double dx = other.x - x;
		final double dy = other.y - y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}
}