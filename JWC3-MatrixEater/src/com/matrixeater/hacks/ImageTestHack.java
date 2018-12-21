package com.matrixeater.hacks;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.lwjgl.Sys;

import com.hiveworkshop.wc3.gui.BLPHandler;

public class ImageTestHack {
	public static void main(final String[] args) {
		final String version = Sys.getVersion();
		System.out.println(version);

		JOptionPane.showMessageDialog(null, new ImageIcon(BLPHandler.get().getCustomTex(
				"/home/etheller/Documents/CASC/war3.mpq/replaceabletextures/worldeditui/editor-triggergroup-open.blp")));
	}
}
