package com.hiveworkshop.wc3.mdl;

import javax.swing.JOptionPane;

public class Normal extends Vertex {
	public Normal(final double x, final double y, final double z) {
		super(x, y, z);
	}

	public Normal(final Normal oldNorm) {
		super(oldNorm.x, oldNorm.y, oldNorm.z);
	}

	public static Normal parseText(final String input) {
		final String[] entries = input.split(",");
		Normal temp = null;
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
		temp = new Normal(x, y, z);
		return temp;
	}

	public void inverse() {
		x = -x;
		y = -y;
		z = -z;
	}
}