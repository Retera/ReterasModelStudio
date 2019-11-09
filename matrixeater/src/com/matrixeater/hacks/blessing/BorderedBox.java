package com.matrixeater.hacks.blessing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class BorderedBox implements FrameHandle {
	private final Rectangle bounds;
	private final BufferedImage borderTexture;
	private final int borderBoxSize;

	public BorderedBox(final Rectangle bounds, final BufferedImage borderTexture) {
		this.bounds = bounds;
		this.borderTexture = borderTexture;
		borderBoxSize = borderTexture.getHeight();
	}

	public void setLocation(final int x, final int y) {
		bounds.x = x;
		bounds.y = y;
	}

	public int getBorderBoxSize() {
		return borderBoxSize;
	}

	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;

		final int borderVerticalRepeatCount = bounds.height / borderBoxSize;
		// first draw corners

		g2.drawImage(borderTexture, bounds.x, bounds.y, bounds.x + borderBoxSize, bounds.y + borderBoxSize,
				borderBoxSize * 4, 0, (borderBoxSize * 4) + borderBoxSize, borderBoxSize, null);
		g2.drawImage(borderTexture, bounds.x, (bounds.y + bounds.height) - borderBoxSize, bounds.x + borderBoxSize,
				bounds.y + bounds.height, borderBoxSize * 6, 0, (borderBoxSize * 6) + borderBoxSize, borderBoxSize,
				null);
		g2.drawImage(borderTexture, (bounds.x + bounds.width) - borderBoxSize, bounds.y, bounds.x + bounds.width,
				bounds.y + borderBoxSize, borderBoxSize * 5, 0, (borderBoxSize * 5) + borderBoxSize, borderBoxSize,
				null);
		g2.drawImage(borderTexture, (bounds.x + bounds.width) - borderBoxSize,
				(bounds.y + bounds.height) - borderBoxSize, bounds.x + bounds.width, bounds.y + bounds.height,
				borderBoxSize * 7, 0, (borderBoxSize * 7) + borderBoxSize, borderBoxSize, null);
		for (int i = 1; i < (borderVerticalRepeatCount - 1); i++) {
			g2.drawImage(borderTexture, bounds.x, bounds.y + (borderBoxSize * i), bounds.x + borderBoxSize,
					bounds.y + (borderBoxSize * i) + borderBoxSize, 0, 0, borderBoxSize, borderBoxSize, null);
			g2.drawImage(borderTexture, (bounds.x + bounds.width) - borderBoxSize, bounds.y + (borderBoxSize * i),
					(bounds.x + bounds.width), bounds.y + (borderBoxSize * i) + borderBoxSize, borderBoxSize, 0,
					borderBoxSize * 2, borderBoxSize, null);
		}
		final int heightRemainder = bounds.height % borderBoxSize;
		g2.drawImage(borderTexture, bounds.x, bounds.y + (borderBoxSize * (borderVerticalRepeatCount - 1)),
				bounds.x + borderBoxSize,
				bounds.y + (borderBoxSize * (borderVerticalRepeatCount - 1)) + heightRemainder, 0, 0, borderBoxSize,
				heightRemainder, null);
		g2.drawImage(borderTexture, (bounds.x + bounds.width) - borderBoxSize,
				bounds.y + (borderBoxSize * (borderVerticalRepeatCount - 1)), (bounds.x + bounds.width),
				bounds.y + (borderBoxSize * (borderVerticalRepeatCount - 1)) + heightRemainder, borderBoxSize, 0,
				borderBoxSize * 2, heightRemainder, null);

		final int borderHorizontalRepeatCount = bounds.width / borderBoxSize;
		for (int i = 1; i < (borderHorizontalRepeatCount - 1); i++) {
			g2.rotate(Math.PI / 2, bounds.x + (borderBoxSize * i) + (borderBoxSize / 2),
					bounds.y + (borderBoxSize / 2));
			g2.drawImage(borderTexture, bounds.x + (borderBoxSize * i), bounds.y,
					bounds.x + borderBoxSize + (borderBoxSize * i), bounds.y + borderBoxSize, borderBoxSize * 2, 0,
					(borderBoxSize * 2) + borderBoxSize, borderBoxSize, null);
			g2.rotate(-Math.PI / 2, bounds.x + (borderBoxSize * i) + (borderBoxSize / 2),
					bounds.y + (borderBoxSize / 2));
			g2.rotate(Math.PI / 2, bounds.x + (borderBoxSize * i) + (borderBoxSize / 2),
					((bounds.y + bounds.height) - borderBoxSize) + (borderBoxSize / 2));
			g2.drawImage(borderTexture, bounds.x + (borderBoxSize * i), (bounds.y + bounds.height) - borderBoxSize,
					bounds.x + borderBoxSize + (borderBoxSize * i), bounds.y + bounds.height, borderBoxSize * 3, 0,
					(borderBoxSize * 3) + borderBoxSize, borderBoxSize, null);
			g2.rotate(-Math.PI / 2, bounds.x + (borderBoxSize * i) + (borderBoxSize / 2),
					((bounds.y + bounds.height) - borderBoxSize) + (borderBoxSize / 2));
		}
	}

	@Override
	public String getTooltip() {
		return null;
	}

	@Override
	public int getCost() {
		return 0;
	}

	@Override
	public String getUbertip() {
		return null;
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}
}
