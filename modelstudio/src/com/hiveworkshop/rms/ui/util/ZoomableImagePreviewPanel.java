package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ZoomableImagePreviewPanel extends JPanel {
	private final Image image;
	private float scale = 1.0f;

	public ZoomableImagePreviewPanel(final Image image) {
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
		if (image != null) {
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
}
