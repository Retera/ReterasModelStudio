package com.requestin8r.src.units;

import java.awt.Component;

import javax.swing.JOptionPane;

public class ModelOptionPane {
	public static String show(Component what) {
		ModelOptionPanel uop = new ModelOptionPanel();
		int x = JOptionPane.showConfirmDialog(what, uop, "Choose Model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if( x == JOptionPane.OK_OPTION) {
			return uop.getSelection();
		}
		return null;
	}
}
