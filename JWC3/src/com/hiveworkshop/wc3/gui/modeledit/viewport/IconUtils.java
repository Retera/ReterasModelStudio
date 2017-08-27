package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class IconUtils {
	public static ImageIcon loadImageIcon(final String path) {
		return new ImageIcon(IconUtils.class.getResource(path));
		// return new ImageIcon(new
		// ImageIcon(IconUtils.class.getResource(path)).getImage().getScaledInstance(16,
		// 16,
		// Image.SCALE_FAST));
	}

	public static Image loadImage(final String path) {
		try {
			return ImageIO.read(IconUtils.class.getResource(path));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IconUtils() {
	}
}
