package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Cool gradient colored JButton
 */
public class ModeButton2 extends JButton {
	GradientPaint gPaint;
	Color activeColor1;
	Color activeColor2;

	public ModeButton2(final String s, Color activeColor1, Color activeColor2) {
		super(s);
		this.activeColor1 = activeColor1;
		this.activeColor2 = activeColor2;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				if (gPaint != null) {
					gPaint = new GradientPaint(
							new Point(0, 10), gPaint.getColor1(),
							new Point(0, getHeight()), gPaint.getColor2(), true);
				}
			}
		});
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (gPaint != null) {
			final Graphics2D g2 = (Graphics2D) g.create();
			g2.setPaint(gPaint);
			final int amt = 4;
			final int indent = 1;
			g2.fillRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3);
			g2.setColor(Color.black);
			g2.drawRoundRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3, amt, amt);
			g2.dispose();
		}
		super.paintComponent(g);
	}

	public void setActive(boolean active){
		if(active) {
			setContentAreaFilled(false);
			gPaint = new GradientPaint(new Point(0, 10), activeColor1, new Point(0, getHeight()), activeColor2, true);
		}
		else {
			gPaint = null;
			setContentAreaFilled(true);
		}
	}

	public void setColors(Color activeColor1, Color activeColor2) {
		this.activeColor1 = activeColor1;
		this.activeColor2 = activeColor2;
	}

	public boolean isColorModeActive() {
		return gPaint != null;
	}
}
