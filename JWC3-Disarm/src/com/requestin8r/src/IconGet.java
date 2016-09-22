package com.requestin8r.src;

import java.awt.Image;

import com.hiveworkshop.wc3.gui.BLPHandler;

public class IconGet {
	public static Image get(final String name, final int size) {
		return BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTN"+name+".blp").getScaledInstance(size, size, Image.SCALE_SMOOTH);
	}
}
