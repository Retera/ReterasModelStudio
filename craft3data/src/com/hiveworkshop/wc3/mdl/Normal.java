package com.hiveworkshop.wc3.mdl;
import hiveworkshop.localizationmanager.LocalizationManager;

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
					LocalizationManager.getInstance().get("global.dialog.error") +" {" + input + "}: " + LocalizationManager.getInstance().get("dialog.normal_parsetext_coordinates"));
		}
		try {
			y = Double.parseDouble(entries[1]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					LocalizationManager.getInstance().get("global.dialog.error") + " {" + input + "}: " + LocalizationManager.getInstance().get("dialog.normal_parsetext_coordinates"));
		}
		try {
			z = Double.parseDouble(entries[2].split("}")[0]);
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					LocalizationManager.getInstance().get("global.dialog.error") + " {" + input + "}: " + LocalizationManager.getInstance().get("dialog.normal_parsetext_coordinates"));
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