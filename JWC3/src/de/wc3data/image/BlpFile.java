package de.wc3data.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class BlpFile {

	public static void writeJpgBLP(final BufferedImage imageData, final File imageFileBLP, final boolean b,
			final float f) {
		try {
			ImageIO.write(imageData, "blp", imageFileBLP);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage read(final File selectedFile) {
		try {
			return ImageIO.read(selectedFile);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writePalettedBLP(final BufferedImage currentImage, final File selectedFile,
			final boolean selected, final boolean selected2, final boolean selected3) {
		try {
			ImageIO.write(currentImage, "blp", selectedFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage read(final String object, final InputStream resourceAsStream) {
		try {
			return ImageIO.read(resourceAsStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
