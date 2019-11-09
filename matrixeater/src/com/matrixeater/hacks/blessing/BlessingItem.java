package com.matrixeater.hacks.blessing;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class BlessingItem implements FrameHandle {
	private final BufferedImage icon;
	private final String tooltip;
	private final int stonesCost;
	private final String ubertip;
	private final Rectangle bounds;

	public BlessingItem(final Rectangle bounds, final BufferedImage icon, final String tooltip, final int stonesCost,
			final String ubertip) {
		this.bounds = bounds;
		this.icon = icon;
		this.tooltip = tooltip;
		this.stonesCost = stonesCost;
		this.ubertip = ubertip;
	}

	@Override
	public void paint(final Graphics g) {
		g.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, null);
	}

	@Override
	public String getTooltip() {
		return tooltip;
	}

	@Override
	public int getCost() {
		return stonesCost;
	}

	@Override
	public String getUbertip() {
		return ubertip;
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}
}
