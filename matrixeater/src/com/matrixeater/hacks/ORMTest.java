package com.matrixeater.hacks;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class ORMTest {

	public static void main(final String[] args) {
		final InputStream dds = MpqCodebase.get().getResourceAsStream(
				"war3.w3mod\\_hd.w3mod\\doodads\\cityscape\\props\\citygrave\\cs_props_grave_v3_diffuse.dds");
		try {
			final BufferedImage read = ImageIO.read(dds);
			JOptionPane.showMessageDialog(null, new ImageIcon(forceBufferedImagesRGB(read)));
		} catch (final HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert an input buffered image into sRGB color space using component values
	 * directly instead of performing a color space conversion.
	 *
	 * @param in Input image to be converted.
	 * @return Resulting sRGB image.
	 */
	public static BufferedImage forceBufferedImagesRGB(final BufferedImage in) {
		// Resolve input ColorSpace.

		// Draw input.
		final BufferedImage lRGB = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < in.getWidth(); i++) {
			for (int j = 0; j < in.getHeight(); j++) {
				lRGB.setRGB(i, j, in.getRGB(i, j));
			}
		}

		return lRGB;
	}
}
