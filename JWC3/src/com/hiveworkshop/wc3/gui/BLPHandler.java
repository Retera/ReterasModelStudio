package com.hiveworkshop.wc3.gui;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.image.BlpFile;

public class BLPHandler {

	public BLPHandler() {

	}

	/**
	 * Caching here is dangerous, only works if you're not changing the underlying images.
	 */
	Map<String, BufferedImage> cache = new HashMap<>();

	public BufferedImage getTexture(final String workingDirectory, final String filepath) {
		final BufferedImage image = getGameTex(filepath);
		if (image != null) {
			return image;
		}
		try {
			final BufferedImage newImage2 = getCustomTex(workingDirectory + "\\" + filepath);
			if (newImage2 != null) {
				return newImage2;
			}
			throw new RuntimeException("Failed to load game texture: " + filepath + " (in " + workingDirectory + ")");
		} catch (final Exception exc2) {
			throw new RuntimeException("Failed to load game texture: " + filepath + " (in " + workingDirectory + ")",
					exc2);
		}
	}

	/**
	 * Convert an input buffered image into sRGB color space using component values directly instead of performing a
	 * color space conversion.
	 *
	 * @param in
	 *            Input image to be converted.
	 * @return Resulting sRGB image.
	 */
	public static BufferedImage forceBufferedImagesRGB(final BufferedImage in) {
		// Resolve input ColorSpace.
		final ColorSpace inCS = in.getColorModel().getColorSpace();
		final ColorSpace sRGBCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		if (inCS == sRGBCS) {
			// Already is sRGB.
			return in;
		}
		if (inCS.getNumComponents() != sRGBCS.getNumComponents()) {
			throw new IllegalArgumentException("Input color space has different number of components from sRGB.");
		}

		// Draw input.
		final ColorModel lRGBModel = new ComponentColorModel(inCS, true, false, Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		final ColorModel sRGBModel = new ComponentColorModel(sRGBCS, true, false, Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		final BufferedImage lRGB = new BufferedImage(lRGBModel,
				lRGBModel.createCompatibleWritableRaster(in.getWidth(), in.getHeight()), false, null);
		final Graphics2D graphic = lRGB.createGraphics();
		try {
			graphic.drawImage(in, 0, 0, null);
		} finally {
			graphic.dispose();
		}

		// Convert to sRGB.
		final BufferedImage sRGB = new BufferedImage(sRGBModel, lRGB.getRaster(), false, null);

		return sRGB;
	}

	/**
	 * Gets a texture file from BLP format inside the Warcraft archives into a BufferedImage you can use, based on a
	 * filepath in the Warcraft installation's MPQ files.
	 *
	 * @param filepath
	 * @return
	 */
	public BufferedImage getGameTex(final String filepath) {
		if (cache.containsKey(filepath)) {
			return cache.get(filepath);
		}
		final InputStream blpFile = MpqCodebase.get().getResourceAsStream(filepath);
		try {
			// final BufferedImage img = BlpFile.read(filepath, blpFile);
			final BufferedImage img = forceBufferedImagesRGB(ImageIO.read(blpFile));
			cache.put(filepath, img);
			return img;// ImageIO.read(tga);
		} catch (final IOException e) {
			// we return null here, swallow exception, be very careful with this
		}
		// final File blpFile = MpqCodebase.get().getFile(filepath);
		// final File tga = convertBLPtoTGA(blpFile);
		//
		// try {
		// final BufferedImage img = TargaReader.getImage(tga.getPath());
		// cache.put(filepath, img);
		// return img;//ImageIO.read(tga);
		// } catch (final IOException e) {
		// e.printStackTrace();
		// }
		return null;
	}

	/**
	 * Returns a BufferedImage from any arbitrary filepath string on your computer, reading the image from BLP format.
	 *
	 * @param filepath
	 * @return
	 */
	public BufferedImage getCustomTex(final String filepath) {
		final File blpFile = new File(filepath);
		final File tga;
		try {
			return BlpFile.read(filepath, new FileInputStream(blpFile));
			// tga = convertBLPtoTGA(blpFile, File.createTempFile("customtex",
			// ".tga"));//+(int)(Math.random()*50)
			// System.out.println(tga.getPath());
			// //mpqlib.TestMPQ.draw(mpqlib.TargaReader.getImage(tga.getPath()));
			// return TargaReader.getImage(tga.getPath());//ImageIO.read(tga);
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static boolean WANT_DESTROY_SAVED_TGAS = true;

	public File convertBLPtoTGA(final File blpFile) {
		try {
			final File fileTGA = new File(blpFile.getPath().substring(0, blpFile.getPath().lastIndexOf(".")) + ".tga");
			try {
				Runtime.getRuntime().exec(new String[] { "blplabcl/blplabcl.exe", "\"" + blpFile.getPath() + "\"",
						"\"" + fileTGA.getPath() + "\"", "-type0", "-q256", "-opt2" }).waitFor();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// BufferedImage bi =
			// ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);

			// new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
			if (WANT_DESTROY_SAVED_TGAS) {
				fileTGA.deleteOnExit();
			}
			return fileTGA;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void compressBLPHopefullyALot(final File blpFile, final File blpOutput, final boolean generateMipMaps) {
		try {
			try {
				Runtime.getRuntime()
						.exec(new String[] { "blplabcl/blplabcl.exe", "\"" + blpFile.getPath() + "\"",
								"\"" + blpOutput.getPath() + "\"", "-type0", "-q25", generateMipMaps ? "-mm8" : "",
								"-opt1", "-opt2" })
						.waitFor();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void compressTGAHopefullyALot(final File blpFile, final File blpOutput) {
		try {
			try {
				Runtime.getRuntime().exec(new String[] { "blplabcl/blplabcl.exe", "\"" + blpFile.getPath() + "\"",
						"\"" + blpOutput.getPath() + "\"", "-type0" }).waitFor();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public File convertBLPtoTGA(final File blpFile, final File fileTGA) {
		try {
			try {
				Runtime.getRuntime().exec(new String[] { "blplabcl/blplabcl.exe", "\"" + blpFile.getPath() + "\"",
						"\"" + fileTGA.getPath() + "\"", "-type0", "-q256", "-opt2" }).waitFor();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// BufferedImage bi =
			// ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);

			// new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
			if (WANT_DESTROY_SAVED_TGAS) {
				fileTGA.deleteOnExit();
			}
			return fileTGA;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public File convertTGAtoBLP(final File blpFile, final File fileTGA) {
		try {
			try {
				Runtime.getRuntime().exec(new String[] { "blplabcl/blplabcl.exe", "\"" + blpFile.getPath() + "\"",
						"\"" + fileTGA.getPath() + "\"", "-type0", "-q100", "-mm8" }).waitFor();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// BufferedImage bi =
			// ImageIO.read(fileTGA);//TargaReader.getImage(fileTGA.getPath());//ImageIO.read(fileTGA);

			// new TestMPQ().drawBlp(bi);//myBLP.getBufferedImage());
			return fileTGA;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BLPHandler current;

	public static BLPHandler get() {
		if (current == null) {
			current = new BLPHandler();
		}
		return current;
	}

	public void dropCache() {
		cache.clear();
	}
}
