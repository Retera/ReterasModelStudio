package com.hiveworkshop.wc3.mdl;

import java.util.List;

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

	public static Vertex centerOfGroup(final List<? extends Vertex> group) {
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
	public Vertex
}
