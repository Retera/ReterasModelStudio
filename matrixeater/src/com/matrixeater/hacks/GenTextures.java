package com.matrixeater.hacks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.gui.BLPHandler;

import de.wc3data.image.TgaFile;

public class GenTextures {

	public static void main(final String[] args) {
		final File dest = new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\1x1ColorTga");
		dest.mkdir();
		for (int i = 0; i < 36; i++) {
			final Color color = colorByAngle(i * 10);
			final BufferedImage img = BLPHandler.get().getGameTex("ReplaceableTextures\\TeamColor\\TeamColor21.blp");
			final Graphics graphics = img.getGraphics();
			graphics.setColor(color);
			graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
			graphics.dispose();
			try {
				TgaFile.writeTGA(img, new File(dest.getPath() + "\\ColorTex" + i + ".tga"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static Color colorByAngle(final double angle) {
		final int red = Math.min(255, Math.max(0, (int) (Math.abs(((180 - angle) * 510) / 120.)) - 255));
		final int green = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 120) * 510) / 120.))));
		final int blue = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 240) * 510) / 120.))));
		return new Color(red, green, blue);
	}
}
