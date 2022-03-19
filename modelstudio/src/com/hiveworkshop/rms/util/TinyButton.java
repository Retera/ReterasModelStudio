package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TinyButton extends JButton {
	GradientPaint gPaint;
	Color activeColor1;
	Color activeColor2;

	public TinyButton(final String s, Color activeColor1, Color activeColor2) {
		super(s);
		setContentAreaFilled(false);
		this.activeColor1 = activeColor1;
		this.activeColor2 = activeColor2;
		int size = getFont().getSize();
		Font font = getFont().deriveFont(size/2f);
		setFont(font);
		setActive(true);
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
	public TinyButton(final String s) {
		super(s);
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
		int height = getHeight();
		Dimension dimension = new Dimension(height, height);
		System.out.println(height);
		setPreferredSize(dimension);
		setSize(dimension);
//		setMaximumSize(dimension);
//		setMinimumSize(dimension);
		final Graphics2D g2 = (Graphics2D) g.create();
		if (gPaint != null) {
			g2.setPaint(gPaint);
		}
		final int amt = 4;
		final int indent = 1;
		g2.fillRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3);
		g2.setColor(Color.black);
		g2.drawRoundRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3, amt, amt);
		g2.dispose();
		super.paintComponent(g);
	}

	public void setColors(Color activeColor1, Color activeColor2) {
		this.activeColor1 = activeColor1;
		this.activeColor2 = activeColor2;
	}



	public void setActive(boolean active){
		if(active) {
			setContentAreaFilled(false);
			gPaint = new GradientPaint(new Point(0, 10), activeColor1, new Point(0, getHeight()), activeColor2, true);
		}
		else {
			gPaint = null;
			setContentAreaFilled(false);
		}
	}
}
