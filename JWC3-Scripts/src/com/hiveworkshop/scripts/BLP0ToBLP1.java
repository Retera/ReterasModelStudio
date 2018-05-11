package com.hiveworkshop.scripts;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

public class BLP0ToBLP1 {
	public static void main(final String[] args) {
		ImageIO.scanForPlugins();
		final File file = new File("input/blp0/LordaeronSummerTree.blp");
		// final File file = new File("input/WarcraftIII-logo-alpha-orig.blp");
		try {
			final Iterator<ImageReader> imageReadersBySuffix = ImageIO.getImageReadersBySuffix("blp");
			final ImageReader firstReader = imageReadersBySuffix.next();
			firstReader.setInput(file);
			final BufferedImage image = firstReader.read(0, firstReader.getDefaultReadParam());
			// final BufferedImage imageFromGame = ImageIO.read(MpqCodebase.get()
			// .getResourceAsStream("UI\\Glues\\MainMenu\\WarCraftIIILogo\\WarcraftIII-logo-alpha.blp"));
			// JOptionPane.showMessageDialog(null, new ImageIcon(image));
			final BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			final Graphics graphics = img.getGraphics();
			graphics.drawImage(image, 0, 0, null);
			graphics.dispose();
			ImageIO.write(img, "blp", new File("output/LordaeronSummerTree.blp"));

			// final BufferedImage imageFromGame = ImageIO
			// .read(MpqCodebase.get().getResourceAsStream("Textures\\Sentinel.blp"));
			// JOptionPane.showMessageDialog(null, new ImageIcon(imageFromGame));
			// ImageIO.write(imageFromGame, "blp", new File("output/SentinelX.blp"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
