package com.hiveworkshop.wc3.mdl;

import java.util.List;

import javax.swing.JOptionPane;

public class TVertex {
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

	public static TVertex centerOfGroup(final List<? extends TVertex> group) {
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
}