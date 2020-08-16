package com.hiveworkshop.wc3.gui.modelviewer;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Stupid {
	public static void main(final String[] args) {

		try {
			final BufferedImage img = ImageIO.read(new FileInputStream(
					"C:/MPQBuild/War3.mpq/war3.mpq/terrainart\\\\lordaeronsummer\\\\lords_dirt.blp"));
			JOptionPane.showMessageDialog(null, new ImageIcon(img));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
