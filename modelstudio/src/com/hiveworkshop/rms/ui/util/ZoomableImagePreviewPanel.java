package com.hiveworkshop.rms.ui.util;

import net.miginfocom.swing.MigLayout;

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
	private boolean alignTop = false;

	public ZoomableImagePreviewPanel(final Image image) {
		super(new MigLayout("fill"));
		this.image = image;

		MouseAdapter mouseAdapter = getMouseAdapter(image);
		addMouseWheelListener(mouseAdapter);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public ZoomableImagePreviewPanel(final Image image, boolean alignTop) {
		this(image);
		this.alignTop = alignTop;
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
				double pow = Math.pow(1.05, -e.getPreciseWheelRotation());
				scale *= pow;
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
			double offX = scale * image.getWidth(null);
			double offY = scale * image.getHeight(null);
			offsetX = (int) Math.max(Math.min(offsetX, offX), -offX);
			offsetY = (int) Math.max(Math.min(offsetY, offY), -offY);
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int imageWidth = image.getWidth(null);
			int imageHeight = image.getHeight(null);

			float scale = getScale(imageWidth, imageHeight);

			int scaledWidth = (int) (scale * imageWidth);
			int x = ((getWidth() - scaledWidth) / 2) - offsetX;

			int scaledHeight = (int) (scale * imageHeight);
			int y = alignTop ? -offsetY : (((getHeight() - scaledHeight) / 2) - offsetY);

			g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
		}
	}

	private float getScale(int imageWidth, int imageHeight) {
		float scaleX = getWidth() / (float) imageWidth;
		float scaleY = getHeight() / (float) imageHeight;
		return Math.min(scaleX, scaleY) * scale;
	}
}
