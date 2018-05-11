package com.hiveworkshop.wc3.gui.mpqbrowser;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

public class BLPPanel extends JPanel {
	private final Image image;
	private float scale = 1.0f;

	public BLPPanel(final Image image) {
		this.image = image;

		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				scale *= Math.pow(1.05, -e.getPreciseWheelRotation());
				repaint();
			}
		});
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final int size = (int) (Math.min(getWidth(), getHeight()) * scale);
		final int imageWidth = image.getWidth(null);
		final int imageHeight = image.getHeight(null);
		final float scale = size / (float) Math.max(imageWidth, imageHeight);
		final int renderWidth = (int) (scale * imageWidth);
		final int x = (getWidth() - renderWidth) / 2;
		final int renderHeight = (int) (scale * imageHeight);
		final int y = (getHeight() - renderHeight) / 2;
		g.drawImage(image, x, y, renderWidth, renderHeight, null);
	}
}
