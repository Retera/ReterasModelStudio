package com.hiveworkshop.rms.util;

import java.awt.*;

public class ScreenInfo {
	static Dimension screenSize;
	static Dimension bigWindow;
	static Dimension smallWindow;

	static {
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screenSize.width;
		int h = screenSize.height;

		int lW = (int) Math.max(w * 0.8, w - 200);
		int lH = (int) Math.max(h * 0.8, h - 200);
		bigWindow = new Dimension(lW, lH);

		int sW = (int) Math.min(w * 0.5, 700);
		int sH = (int) Math.min(h * 0.5, 500);
		smallWindow = new Dimension(sW, sH);
	}

	public static Dimension getFullScreenSize() {
		return screenSize;
	}

	public static Dimension getBigWindow() {
		return bigWindow;
	}

	public static Dimension getSmallWindow() {
		return smallWindow;
	}
}
