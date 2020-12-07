package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import de.wc3data.image.TgaFile;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BLPHandler {

	public BLPHandler() {
	}

	/**
	 * Caching here is dangerous, only works if you're not changing the underlying
	 * images.
	 */
	Map<String, BufferedImage> cache = new HashMap<>();
	Map<String, GPUReadyTexture> gpuBufferCache = new HashMap<>();
	private static final int BYTES_PER_PIXEL = 4;

	public GPUReadyTexture loadTexture(final DataSource dataSource, final String filepath) {
		final String lowerFilePath = filepath.toLowerCase();
		GPUReadyTexture gpuReadyTexture = gpuBufferCache.get(lowerFilePath);
		if (gpuReadyTexture != null) {
			return gpuReadyTexture;
		}

		final BufferedImage javaTexture = getTexture(dataSource, filepath);

		if (javaTexture == null) {
			return null;
		}

		final int[] pixels = new int[javaTexture.getWidth() * javaTexture.getHeight()];
		javaTexture.getRGB(0, 0, javaTexture.getWidth(), javaTexture.getHeight(), pixels, 0, javaTexture.getWidth());

		final ByteBuffer buffer = BufferUtils
				.createByteBuffer(javaTexture.getWidth() * javaTexture.getHeight() * BYTES_PER_PIXEL);
		// 4
		// for
		// RGBA,
		// 3
		// for
		// RGB

		for (int y = 0; y < javaTexture.getHeight(); y++) {
			for (int x = 0; x < javaTexture.getWidth(); x++) {
				final int pixel = pixels[(y * javaTexture.getWidth()) + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip();

		gpuReadyTexture = new GPUReadyTexture(buffer, javaTexture.getWidth(), javaTexture.getHeight());
		if (cache.containsKey(lowerFilePath)) {
			// In this case, caching is allowed
			gpuBufferCache.put(lowerFilePath, gpuReadyTexture);
		}
		// You now have a ByteBuffer filled with the color data of each pixel.
		return gpuReadyTexture;
	}

	public BufferedImage getTexture(final DataSource dataSource, final String filepath) {
		try {
			final String lowerCaseFilepath = filepath.toLowerCase();
			BufferedImage resultImage = cache.get(lowerCaseFilepath);
			if (resultImage != null) {
				return resultImage;
			}
			if (lowerCaseFilepath.endsWith(".blp") || lowerCaseFilepath.endsWith(".tif")) {
				// War3 allows .blp and .tif to actually resolve to dds
				final String ddsFilepath = filepath.substring(0, filepath.length() - 4) + ".dds";
				if(dataSource.has(ddsFilepath)) {
					resultImage = loadTextureDirectly(dataSource, ddsFilepath);
					if (resultImage != null) {
						if (dataSource.allowDownstreamCaching(ddsFilepath)) {
							cache.put(lowerCaseFilepath, resultImage);
						}
						return resultImage;
					}
				}
			}
			if (dataSource.has(filepath)) {
				resultImage = loadTextureDirectly(dataSource, filepath);
				if (resultImage != null) {
					if (dataSource.allowDownstreamCaching(filepath)) {
						cache.put(lowerCaseFilepath, resultImage);
					}
					return resultImage;
				}
			}
			final String nameOnly = filepath
					.substring(Math.max(filepath.lastIndexOf("/"), filepath.lastIndexOf("\\")) + 1);
			resultImage = loadTextureDirectly(dataSource, nameOnly);
			if (resultImage != null) {
				if (dataSource.allowDownstreamCaching(nameOnly)) {
					cache.put(lowerCaseFilepath, resultImage);
				}
			}
			return resultImage;
		} catch (final IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	private BufferedImage loadTextureDirectly(final DataSource dataSource, final String filepath) throws IOException {
		BufferedImage resultImage = null;
		try (final InputStream imageDataStream = dataSource.getResourceAsStream(filepath)) {
			if (imageDataStream != null) {
				if (isExtension(filepath, ".tga")) {
					resultImage = TgaFile.readTGA(filepath, imageDataStream);
				} else {
					resultImage = ImageIO.read(imageDataStream);
					if (resultImage != null) {
						if (isExtension(filepath, ".blp")) {
							resultImage = forceBufferedImagesRGB(resultImage);
						}
					}
				}
			}
		}
		return resultImage;
	}

	private boolean isExtension(final String filepath, final String extension) {
		if (filepath.length() < extension.length()) {
			return false;
		}
		return filepath.substring(filepath.length() - extension.length()).toLowerCase().equals(extension);
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
		for (int i = 0; i < in.getWidth(); i++) {
			for (int j = 0; j < in.getHeight(); j++) {
				lRGB.setRGB(i, j, in.getRGB(i, j));
			}
		}

		// Convert to sRGB.

		return new BufferedImage(sRGBModel, lRGB.getRaster(), false, null);
	}

// Legacy API
	public static BufferedImage readCustom(final File file) throws IOException {
		final ImageInputStream stream = new FileImageInputStream(file);
		if (stream == null) {
			throw new IllegalArgumentException("stream == null!");
		}

		final Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
		if (!iter.hasNext()) {
			if (file.getName().toLowerCase().endsWith(".tga")) {
				return TgaFile.readTGA(file);
			}
			return null;
		}

		final ImageReader reader = iter.next();
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

	public static BufferedImage getImage(final Bitmap defaultTexture, final DataSource workingDirectory) {
		String path = defaultTexture.getPath();
		if ((path == null) || path.isEmpty()) {
			if (defaultTexture.getReplaceableId() == 1) {
				path = "ReplaceableTextures\\TeamColor\\TeamColor" + Material.getTeamColorNumberString() + ".blp";
			} else if (defaultTexture.getReplaceableId() == 2) {
				path = "ReplaceableTextures\\TeamGlow\\TeamGlow" + Material.getTeamColorNumberString() + ".blp";
			} else if (defaultTexture.getReplaceableId() != 0) {
				path = "replaceabletextures\\lordaerontree\\lordaeronsummertree" + ".blp";
			}
		}
		return BLPHandler.get().getTexture(workingDirectory, path);
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

	public BufferedImage getGameTex(final String iconTexturePath) {
		return getTexture(GameDataFileSystem.getDefault(), iconTexturePath);
	}

	public static BufferedImage removeAlphaChannel(final BufferedImage source) {
		final BufferedImage combined = new BufferedImage(source.getWidth(), source.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);

		return combined;
	}

	public static void exportBitmapTextureFile(final Component component, final ModelView modelView,
			final Bitmap selectedValue, final File file) {
		if (file.exists()) {
			final int confirmOption = JOptionPane.showConfirmDialog(component,
					"File \"" + file.getPath() + "\" already exists. Continue?", "Confirm Export",
					JOptionPane.YES_NO_OPTION);
			if (confirmOption == JOptionPane.NO_OPTION) {
				return;
			}
		}
		final DataSource wrappedDataSource = modelView.getModel().getWrappedDataSource();
		final File workingDirectory = modelView.getModel().getWorkingDirectory();
		BufferedImage bufferedImage = getImage(selectedValue, wrappedDataSource);
		String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase();
		if (fileExtension.equals("BMP") || fileExtension.equals("JPG") || fileExtension.equals("JPEG")) {
			JOptionPane.showMessageDialog(component,
					"Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
			bufferedImage = removeAlphaChannel(bufferedImage);
		}
		if (fileExtension.equals("BLP")) {
			fileExtension = "blp";
		}
		boolean directExport = false;
		if (selectedValue.getPath().toLowerCase().endsWith(fileExtension)) {
			final CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
			if (gameDataFileSystem.has(selectedValue.getPath())) {
				final InputStream mpqFile = gameDataFileSystem.getResourceAsStream(selectedValue.getPath());
				try {
					Files.copy(mpqFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
					directExport = true;
				} catch (final IOException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
				}
			} else {
				if (workingDirectory != null) {
					final File wantedFile = new File(
							workingDirectory.getPath() + File.separatorChar + selectedValue.getPath());
					if (wantedFile.exists()) {
						try {
							Files.copy(wantedFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
							directExport = true;
						} catch (final IOException e) {
							e.printStackTrace();
							ExceptionPopup.display(e);
						}
					}
				}

			}
		}
		if (!directExport) {
			final boolean write;
			try {
				write = ImageIO.write(bufferedImage, fileExtension, file);
				if (!write) {
					JOptionPane.showMessageDialog(component, "File type unknown or unavailable");
				}
			} catch (final IOException e) {
				e.printStackTrace();
				ExceptionPopup.display(e);
			}
		}
	}

	/**
	 * Returns a BufferedImage from any arbitrary filepath string on your computer,
	 * reading the image from BLP format.
	 */
	public BufferedImage getCustomTex(final String filepath) {
		final File blpFile = new File(filepath);
		final File tga;
		try {
			if (filepath.toLowerCase().endsWith(".blp")) {
				final BufferedImage rawImage = readCustom(blpFile);
				return forceBufferedImagesRGB(rawImage);// BlpFile.read(filepath, new FileInputStream(blpFile));
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
}
