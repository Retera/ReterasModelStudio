package com.hiveworkshop.wc3.mdl;

public final class MdlxUtils {
	public static float[] flipRGBtoBGR(final float[] rgb) {
		final float[] bgr = new float[3];
		for (int i = 0; i < 3; i++) {
			bgr[i] = rgb[2 - i];
		}
		return bgr;
	}
}
