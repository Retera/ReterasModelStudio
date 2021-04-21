package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ZoomableImagePreviewPanel extends JPanel {
	private final Image image;
	private float scale = 1.0f;
	private int offsetX = 0;
	private int offsetY = 0;

	public ZoomableImagePreviewPanel(final Image image) {
		this.image = image;

		MouseAdapter mouseAdapter = getMouseAdapter(image);
		addMouseWheelListener(mouseAdapter);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	private MouseAdapter getMouseAdapter(Image image) {
		return new MouseAdapter() {
			Point start;
			boolean mouseDown;
			int offX = 0;
			int offY = 0;

			@Override
			public void mousePressed(MouseEvent e) {
				if (!mouseDown) {
					mouseDown = true;
					start = e.getPoint();
					offX = offsetX;
					offY = offsetY;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scale *= Math.pow(1.05, -e.getPreciseWheelRotation());
				adjustOffset(image);
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				offsetX = start.x - e.getX() + offX;
				offsetY = start.y - e.getY() + offY;
				adjustOffset(image);
				repaint();
			}
		};
	}

	private void adjustOffset(Image image) {
		if (image != null) {
			double offX = image.getWidth(null) * scale * .2;
			double offY = image.getHeight(null) * scale * .2;
			offsetX = (int) Math.max(Math.min(offsetX, offX), -offX);
			offsetY = (int) Math.max(Math.min(offsetY, offY), -offY);
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int size = (int) (Math.min(getWidth(), getHeight()) * scale);
			int imageWidth = image.getWidth(null);
			int imageHeight = image.getHeight(null);
			float scale = size / (float) Math.max(imageWidth, imageHeight);
			int renderWidth = (int) (scale * imageWidth);
			int x = (getWidth() - renderWidth) / 2 - offsetX;
			int renderHeight = (int) (scale * imageHeight);
			int y = (getHeight() - renderHeight) / 2 - offsetY;
			g.drawImage(image, x, y, renderWidth, renderHeight, null);
		}
	}
}
