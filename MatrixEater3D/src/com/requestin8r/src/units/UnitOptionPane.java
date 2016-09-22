package com.requestin8r.src.units;

import java.awt.Component;

import javax.swing.JOptionPane;

public class UnitOptionPane {
	public static Unit show(Component what) {
		UnitOptionPanel uop = new UnitOptionPanel();
		int x = JOptionPane.showConfirmDialog(what, uop, "Choose Unit Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if( x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}
}
