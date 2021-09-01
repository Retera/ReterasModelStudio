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
	int imageWidth = 100;
	int imageHeight = 100;

	public ZoomableImagePreviewPanel(final Image image) {
		super(new MigLayout("fill"));
		this.image = image;
		if (image != null) {
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);
		}

		MouseAdapter mouseAdapter = getMouseAdapter();
		addMouseWheelListener(mouseAdapter);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public ZoomableImagePreviewPanel(final Image image, boolean alignTop) {
		this(image);
		this.alignTop = alignTop;
	}

	private MouseAdapter getMouseAdapter() {
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


////				double offX = image.getWidth(null) * scale * .2;
////				double offY = image.getHeight(null) * scale * .2;
////				offsetX = (int) Math.max(Math.min(offsetX, offX), -offX);
////				offsetY = (int) Math.max(Math.min(offsetY, offY), -offY);
//
////				double wr = e.getPreciseWheelRotation();
////
//				int dir = e.getPreciseWheelRotation() < 0 ? -1 : 1;
//
////				double mouseX = e.getX();
////				double mouseY = e.getY();
//
//				if(dir<0){
//					double zoomAdjust = (1-pow) * dir / pow;
////				double zoomAdjust = 1;
//
//					double w = e.getX() - (getWidth() / 1.0) ;
//					double h = e.getY() - (getHeight() / 1.0);
//
//					offsetX += w * zoomAdjust / scale / (getWidth()/(float)getHeight());
//					offsetY += h * zoomAdjust / scale;
//				} else {
//					offsetX -= offsetX/scale;
//					offsetY -= offsetY/scale;
//				}
//				adjustOffset(image);
//				repaint();
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
			double offX = scale * imageWidth;
			double offY = scale * imageHeight;
			offsetX = (int) Math.max(Math.min(offsetX, offX), -offX);
			offsetY = (int) Math.max(Math.min(offsetY, offY), -offY);
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			float scale = getScale();

			int scaledWidth = (int) (scale * imageWidth);
			int x = ((getWidth() - scaledWidth) / 2) - offsetX;

			int scaledHeight = (int) (scale * imageHeight);
			int y = alignTop ? -offsetY : (((getHeight() - scaledHeight) / 2) - offsetY);

			g.drawImage(image, x, y, scaledWidth, scaledHeight, null);
		}
	}

	private float getScale() {
		float scaleX = getWidth() / (float) imageWidth;
		float scaleY = getHeight() / (float) imageHeight;
		return Math.min(scaleX, scaleY) * scale;
	}
}
