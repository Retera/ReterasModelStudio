package com.hiveworkshop.rms.util;

import java.awt.*;

public class ScreenInfo {
	static Dimension screenSize;
	static Dimension bigWindow;
	static Dimension smallWindow;
	static int w;
	static int h;

	static {
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		w = screenSize.width;
		h = screenSize.height;

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

	public static Dimension getSuitableSize(int with, int height, double fractionIsh) {
		int dW = with;
		int dH = height;
		if (with * 1.2 > fractionIsh * w) {
			dW = (int) (fractionIsh * w);
		} else if (with > w) {
			dW = w;
		}
		if (height * 1.2 > fractionIsh * h) {
			dH = (int) (fractionIsh * h);
		} else if (height > h) {
			dH = h;
		}
		return new Dimension(dW, dH);
	}

	;
}
