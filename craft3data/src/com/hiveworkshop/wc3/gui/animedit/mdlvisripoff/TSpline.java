package com.hiveworkshop.wc3.gui.animedit.mdlvisripoff;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

import com.hiveworkshop.wc3.mdl.AnimFlag;

public class TSpline extends JPanel {
	private final TTan der; // in mdlvis this was just called der, and whatever, I'm copying them right now

	public TSpline(final TTan der) {
		this.der = der;
		setBackground(Color.WHITE);
//		der = new TTan();
//		der.cur = new AnimFlag.Entry(null, null);
//		der.next = new AnimFlag.Entry(null, null);
//		der.prev = new AnimFlag.Entry(null, null);
//		der.tang = new AnimFlag.Entry(null, null);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
	}

	public void render(final int i, final AnimFlag.Entry itd, final AnimFlag.Entry its, final Rectangle rect) {

	}
}
