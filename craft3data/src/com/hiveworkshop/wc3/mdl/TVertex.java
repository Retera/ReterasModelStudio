package com.hiveworkshop.wc3.mdl;

import java.util.Collection;

import javax.swing.JOptionPane;

public class TVertex {
	public static final TVertex ORIGIN = new TVertex(0, 0);
	GeosetVertex parent;
	public double x = 0;
	public double y = 0;

	public TVertex(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public TVertex(final TVertex old) {
		this.x = old.x;
		this.y = old.y;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public void setY(final double y) {
		this.y = y;
	}

	/**
	 * This method was designed late and is not reliable unless updated by an
	 * outside source.
	 *
	 * @param gv
	 */
	public void setParent(final GeosetVertex gv) {
		parent = gv;
	}

	/**
	 * This method was designed late and is not reliable unless updated by an
	 * outside source.
	 *
	 * @return
	 */
	public GeosetVertex getParent() {
		return parent;
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
				x = value;
				break;
			case 1:
				y = value;
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

	public void setTo(final TVertex v) {
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
		final double dx = this.x - centerX;
		final double dy = this.y - centerY;
		this.x = centerX + (dx * scaleX);
		this.y = centerY + (dy * scaleY);
	}

	public void rotate(final double centerX, final double centerY, final double radians, final byte firstXYZ,
			final byte secondXYZ) {
		rotateVertex(centerX, centerY, radians, firstXYZ, secondXYZ, this);
	}

	public static void rotateVertex(final double centerX, final double centerY, final double radians,
			final byte firstXYZ, final byte secondXYZ, final TVertex vertex) {
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

	public static TVertex parseText(final String input) {
		final String[] entries = input.split(",");
		TVertex temp = null;
		double x = 0;
		double y = 0;
		try {
			x = Double.parseDouble(entries[0].split("\\{")[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		try {
			y = Double.parseDouble(entries[1].split("}")[0]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: Vertex coordinates could not be interpreted.");
		}
		temp = new TVertex(x, y);
		return temp;
	}

	@Override
	public String toString() {
		return "{ " + MDLReader.doubleToString(x) + ", " + MDLReader.doubleToString(y) + " }";
	}

	public static TVertex centerOfGroup(final Collection<? extends TVertex> group) {
		double xTot = 0;
		double yTot = 0;
		for (final TVertex v : group) {
			xTot += v.getX();
			yTot += v.getY();
		}
		xTot /= group.size();
		yTot /= group.size();
		return new TVertex(xTot, yTot);
	}

	public double distance(final TVertex other) {
		final double dx = other.x - x;
		final double dy = other.y - y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}
}