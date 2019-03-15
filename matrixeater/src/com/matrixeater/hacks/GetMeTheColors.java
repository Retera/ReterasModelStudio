package com.matrixeater.hacks;

import java.awt.image.BufferedImage;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.resources.WEString;

public class GetMeTheColors {
	public static void main(final String[] args) {
		for (int i = 0; i < 25; i++) {
			String integerText = Integer.toString(i);
			if (integerText.length() < 2) {
				integerText = "0" + integerText;
			}
			final BufferedImage gameTex = BLPHandler.get()
					.getGameTex("ReplaceableTextures\\TeamColor\\TeamColor" + integerText + ".blp");
			final int centerRGB = gameTex.getRGB(gameTex.getWidth() / 2, gameTex.getHeight() / 2);

			final String colorName = WEString.getString("WESTRING_UNITCOLOR_" + integerText);
			final String hexString = Integer.toHexString(centerRGB);
			System.out.println("|c" + hexString.toUpperCase());
//			if (i != 0) {
//				System.out.print("else");
//			}
//			System.out.println("if playerIndex == " + i + " then");
//			System.out.println("    // " + colorName + " player color code:");
//			System.out.println("    call BlzSetSpecialEffectColor(fx, " + ((centerRGB & 0xFF0000) >> 16) + ", "
//					+ ((centerRGB & 0xFF00) >> 8) + ", " + (centerRGB & 0xFF) + ")");
		}
//		System.out.println("endif");
	}
}
