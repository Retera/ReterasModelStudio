package com.matrixeater.hacks.converts;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JOptionPane;

import com.hiveworkshop.blizzard.blp.BLPReader;
import com.hiveworkshop.blizzard.blp.BLPReaderSpi;
import com.hiveworkshop.blizzard.blp.BLPWriteParam;
import com.hiveworkshop.blizzard.blp.BLPWriter;
import com.hiveworkshop.blizzard.blp.BLPWriterSpi;

public class GigapixelHelper {

	public static int howManyMipmaps(final File file) {
		final BLPReader reader = new BLPReader(new BLPReaderSpi());
		try {
			try (FileImageInputStream fileImageInputStream = new FileImageInputStream(file);) {
				reader.setInput(fileImageInputStream);
				try {
					reader.read(0, reader.getDefaultReadParam());
					return reader.getNumImages(true);
				} finally {
					reader.dispose();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	public static void writeMipmaps(final BufferedImage image, final File file, final boolean mipmaps) {
		final BLPWriterSpi blpWriterSpi = new BLPWriterSpi();
		final BLPWriter writer = new BLPWriter(blpWriterSpi);
		try {
			try (FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(file)) {
				writer.setOutput(fileImageOutputStream);
				final ImageWriteParam defaultWriteParam = writer.getDefaultWriteParam();
				((BLPWriteParam) defaultWriteParam).setAutoMipmap(mipmaps);
				writer.write(image);
			}
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
//		process(new File("C:/MPQBuild/War3.mpq"));
		process2(new File("C:\\War3GigapixelTest\\output"));
	}

	public static void process2(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				process2(subFile);
			}
		} else {
			if (file.getPath().endsWith(".png")) {
				final String newPath = file.getName().replace("__", "/");

				final boolean mipmaps = howManyMipmaps(new File(extension("C:/" + newPath, "blp"))) > 1;
				final File output = new File(extension("C:\\War3GigapixelTest\\compiled\\" + newPath, "blp"));
				output.getParentFile().mkdirs();
				try (FileInputStream inpustra = new FileInputStream(file)) {
					writeMipmaps(ImageIO.read(inpustra), output, mipmaps);
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void process(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				process(subFile);
			}
		} else {
			if (file.getPath().endsWith(".blp")) {
				try {
					final BufferedImage image = ImageIO.read(file);
					if (file.getAbsolutePath().contains("__")) {
						System.err.println(file);
						JOptionPane.showMessageDialog(null, file.getAbsolutePath());
					}
					String replacePath = file.getAbsolutePath().replace("/", "__").replace("\\", "__").replace("C:__",
							"C:/War3GigapixelTest/input2/");
					replacePath = replacePath.substring(0, replacePath.lastIndexOf('.')) + ".png";
					final File target = new File(replacePath);
					target.getParentFile().mkdirs();
					System.out.println(target);
					ImageIO.write(image, "png", target);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String extension(final String input, final String ext) {
		final int dotIndex = input.lastIndexOf('.');
		if (dotIndex == -1) {
			return input;
		} else {
			return input.substring(0, dotIndex) + "." + ext;
		}
	}
}
