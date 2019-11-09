package com.hiveworkshop.wc3.gui;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.hiveworkshop.wc3.gui.dds.DDSFile;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class BLPHandler {

	public BLPHandler() {
	}

	/**
	 * Caching here is dangerous, only works if you're not changing the underlying
	 * images.
	 */
	Map<String, BufferedImage> cache = new HashMap<>();
	Map<String, BufferedImage> rawCache = new HashMap<>();

	public BufferedImage getTexture(final String workingDirectory, final String filepath) {
		return getTexture(workingDirectory, filepath, false);
	}

	public BufferedImage getTexture(final String workingDirectory, final String filepath, final boolean alpha) {
		final BufferedImage image = getGameTex(filepath, alpha);
		if (image != null) {
			return image;
		}
		try {
			try {
				final BufferedImage newImage2 = getCustomTex(workingDirectory + File.separatorChar + filepath, alpha);
				if (newImage2 != null) {
					return newImage2;
				}
			} catch (final Exception exc3) {
			}
			final String lastHopePath = workingDirectory + File.separator
					+ filepath.substring(filepath.lastIndexOf(File.separatorChar) + 1);
			final BufferedImage newImage3 = getCustomTex(lastHopePath, alpha);
			if (newImage3 != null) {
				return newImage3;
			}
			return null;
//			throw new RuntimeException("Failed to load game texture: " + filepath + " (in " + workingDirectory + ")");
		} catch (final Exception exc2) {
			throw new RuntimeException("Failed to load game texture: " + filepath + " (in " + workingDirectory + ")",
					exc2);
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
		if (in.getColorModel() == null) {
			return in;
		}
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
	 * Gets a texture file from BLP format inside the Warcraft archives into a
	 * BufferedImage you can use, based on a filepath in the Warcraft installation's
	 * MPQ files.
	 *
	 * @param filepath
	 * @return
	 */
	public BufferedImage getGameTex(final String filepath, final boolean alpha) {
		final Map<String, BufferedImage> cacheToUse = alpha ? rawCache : cache;
		if (cacheToUse.containsKey(filepath)) {
			return cacheToUse.get(filepath);
		}
		InputStream blpFile = MpqCodebase.get().getResourceAsStream(filepath);
		if (blpFile == null) {
			return null;
		}
		try {
			// final BufferedImage img = BlpFile.read(filepath, blpFile);
			final BufferedImage rawImage = ImageIO.read(blpFile);
			if (rawImage == null) {
//				final BLPReader blpReader = new BLPReader(new BLPReaderSpi());
//				blpReader.setInput(blpFile);
//				final Iterator<IIOImage> readAll = blpReader
//						.readAll(Collections.<ImageReadParam>emptyList().iterator());
//				while (readAll.hasNext()) {
//					final IIOImage next = readAll.next();
//					final List<? extends BufferedImage> thumbnails = next.getThumbnails();
//					return thumbnails.get(0);
//				}
				blpFile = MpqCodebase.get().getResourceAsStream(filepath);
				if (DDSFile.isValidDDSImage(blpFile)) {
					final DDSFile image = new DDSFile(filepath);
					if ((image.getTextureType() == DDSFile.TextureType.CUBEMAP)
							|| (image.getTextureType() == DDSFile.TextureType.VOLUME)) {
						System.err.println("Error from DDS: "
								+ "<html>Error: This programm doesn't support cubemaps or volume textures." + "<br>"
								+ image.getFile().getName() + " can not be loaded.</html>");
						return null;
					}
					try {
						image.loadImageData();
					} catch (final ArrayIndexOutOfBoundsException exc2) {
						// currently this is the normal maps case, bad dirty handling
						return null;
					}
					return image.getData();
				}
				return null;
			}
			if (alpha) {
				cacheToUse.put(filepath, rawImage);
				return rawImage;
			}
			final BufferedImage img = forceBufferedImagesRGB(rawImage);
			cacheToUse.put(filepath, img);
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

	public BufferedImage getGameTex(final String filepath) {
		return getGameTex(filepath, false);
	}

	public static BufferedImage readCustom(final File file) throws IOException {
		final ImageInputStream stream = new FileImageInputStream(file);
		if (stream == null) {
			throw new IllegalArgumentException("stream == null!");
		}

		final Iterator iter = ImageIO.getImageReaders(stream);
		if (!iter.hasNext()) {
			return null;
		}

		final ImageReader reader = (ImageReader) iter.next();
		final ImageReadParam param = reader.getDefaultReadParam();
		reader.setInput(file, true, true);
		BufferedImage bi;
		try {
			bi = reader.read(0, param);
		} finally {
			reader.dispose();
			stream.close();
		}
		return bi;
	}

	/**
	 * Returns a BufferedImage from any arbitrary filepath string on your computer,
	 * reading the image from BLP format.
	 *
	 * @param filepath
	 * @return
	 */
	public BufferedImage getCustomTex(final String filepath, final boolean alpha) {
		final File blpFile = new File(filepath);
		final File tga;
		try {
			if (filepath.toLowerCase().endsWith(".blp")) {
				final BufferedImage rawImage = readCustom(blpFile);
				if (alpha) {
					return rawImage;
				}
				final BufferedImage img = forceBufferedImagesRGB(rawImage);
				return img;// BlpFile.read(filepath, new FileInputStream(blpFile));
				// tga = convertBLPtoTGA(blpFile, File.createTempFile("customtex",
				// ".tga"));//+(int)(Math.random()*50)
				// System.out.println(tga.getPath());
				// //mpqlib.TestMPQ.draw(mpqlib.TargaReader.getImage(tga.getPath()));
				// return TargaReader.getImage(tga.getPath());//ImageIO.read(tga);
			} else {
				if (!blpFile.exists()) {
					return null;
				}
				return ImageIO.read(blpFile);
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public BufferedImage getCustomTex(final String filepath) {
		return getCustomTex(filepath, false);
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
		rawCache.clear();
	}

}
