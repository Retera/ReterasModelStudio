package com.hiveworkshop.wc3.gui.modeledit.viewport;

import javax.swing.ImageIcon;

public final class IconUtils {
	public static ImageIcon loadImageIcon(final String path) {
		return new ImageIcon(IconUtils.class.getResource(path));
		// return new ImageIcon(new
		// ImageIcon(IconUtils.class.getResource(path)).getImage().getScaledInstance(16,
		// 16,
		// Image.SCALE_FAST));
	}

	private IconUtils() {
	}
}
