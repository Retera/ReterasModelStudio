package com.hiveworkshop.wc3.units;

import java.awt.Component;

import javax.swing.JOptionPane;

public class UnitOptionPane {
	public static GameObject show(final Component what) {
		final UnitOptionPanel uop = new UnitOptionPanel(DataTable.get(), StandardObjectData.getStandardAbilities());
		final int x = JOptionPane.showConfirmDialog(what, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}

	public static GameObject show(final Component what, final ObjectData table) {
		final UnitOptionPanel uop = new UnitOptionPanel(table, StandardObjectData.getStandardAbilities());
		final int x = JOptionPane.showConfirmDialog(what, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}
}
