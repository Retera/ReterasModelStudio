package com.hiveworkshop.wc3.mdl;

import java.util.Collection;

import javax.swing.JOptionPane;

public class Vertex {
	private static final Vertex ORIGIN = new Vertex(0, 0, 0);
	public double x = 0;
	public double y = 0;
	public double z = 0;

	public Vertex(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vertex(final Vertex v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vertex(final float[] data) {
		x = data[0];
		y = data[1];
		z = data[2];
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

	public void setCoord(final byte dim, final double value) {
		if (!Double.isNaN(value)) {
			switch (dim) {
			case 0:
				x = value;
				break;
			case 1:
				y = value;
				break;
			case 2:
				z = value;
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

	public void setTo(final Vertex v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public boolean equalLocs(final Vertex v) {
		return x == v.x && y == v.y && z == v.z;
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

	public static Vertex parseText(final String input) {
		final String[] entries = input.split(",");
		Vertex temp = null;
		double x = 0;
		double y = 0;
		double z = 0;
		try {
			x = Double.parseDouble(entries[0].split("\\{")[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			y = Double.parseDouble(entries[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			z = Double.parseDouble(entries[2].split("}")[0]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		temp = new Vertex(x, y, z);
		return temp;
	}

	@Override
	public String toString() {
		return "{ " + MDLReader.doubleToString(x) + ", " + MDLReader.doubleToString(y) + ", "
				+ MDLReader.doubleToString(z) + " }";
	}

	public String toStringLessSpace() {
		return "{" + MDLReader.doubleToString(x) + ", " + MDLReader.doubleToString(y) + ", "
				+ MDLReader.doubleToString(z) + "}";
	}

	public double[] toArray() {
		return new double[] { x, y, z };
	}

	public float[] toFloatArray() {
		return new float[] { (float) x, (float) y, (float) z };
	}

	public static Vertex centerOfGroup(final Collection<? extends Vertex> group) {
		double xTot = 0;
		double yTot = 0;
		double zTot = 0;
		for (final Vertex v : group) {
			xTot += v.getX();
			yTot += v.getY();
			zTot += v.getZ();
		}
		xTot /= group.size();
		yTot /= group.size();
		zTot /= group.size();
		return new Vertex(xTot, yTot, zTot);
	}

	public double distance(final Vertex other) {
		final double dx = other.x - x;
		final double dy = other.y - y;
		final double dz = other.z - z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public double vectorMagnitude() {
		return distance(ORIGIN);
	}

	public Vertex crossProduct(final Vertex other) {
		final double x2 = other.x;
		final double y2 = other.y;
		final double z2 = other.z;
		return crossProduct(x2, y2, z2);
	}

	private Vertex crossProduct(final double x2, final double y2, final double z2) {
		return new Vertex(y * z2 - y2 * z, x2 * z - x * z2, x * y2 - x2 * y);
	}

	public void translate(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void scale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		final double dx = this.x - centerX;
		final double dy = this.y - centerY;
		final double dz = this.z - centerZ;
		this.x = centerX + dx * scaleX;
		this.y = centerY + dy * scaleY;
		this.z = centerZ + dz * scaleZ;
	}

	public void rotate(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		rotateVertex(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, this);
	}

	public static void rotateVertex(final Vertex center, final Vertex axis, final double radians, final Vertex vertex) {
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
			vertex.x = centerX - deltaX;
			vertex.y = centerY - deltaY;
			vertex.z = centerY - deltaZ;
		}
		final double resultDeltaX = vertexX * cosRadians;
		throw new UnsupportedOperationException("NYI");
	}

	public static void rotateVertex(final double centerX, final double centerY, final double centerZ,
			final double radians, final byte firstXYZ, final byte secondXYZ, final Vertex vertex) {
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
		final double r = Math.sqrt(dx * dx + dy * dy);
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double nextDim = Math.cos(verAng + radians) * r + cx;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(firstXYZ, Math.cos(verAng + radians) * r + cx);
		}
		// if( getDimEditable(dim2) )
		nextDim = Math.sin(verAng + radians) * r + cy;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(secondXYZ, Math.sin(verAng + radians) * r + cy);
		}
	}
}
