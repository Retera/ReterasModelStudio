package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import de.wc3data.image.TgaFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BLPHandler {

	public BLPHandler() {
	}

	/**
	 * Caching here is dangerous, only works if you're not changing the underlying
	 * images.
	 */
	Map<String, ImageThingiHelper> cache = new HashMap<>();
	Map<String, ImageThingiHelper> nonCache = new HashMap<>();
	private static final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	static {
		((DataBufferInt) blankImage.getRaster().getDataBuffer()).getData()[0] = 16777215;
	}
	private static final int BYTES_PER_PIXEL = 4;

	private static BLPHandler current;

	public static BLPHandler get() {
		if (current == null) {
			current = new BLPHandler();
		} else if (current.cache.size() > 1056) {
			System.out.println("Dropping texture Cache" + current.cache.size());
			current.dropCache();
		}
		return current;
	}

	public void dropCache() {
		cache.clear();
		nonCache.clear();
	}

	public static BufferedImage getGameTex(String iconTexturePath) {
		return get().getTexture(GameDataFileSystem.getDefault(), iconTexturePath);
	}

	public static BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory) {
		return BLPHandler.get().getTexture(workingDirectory, bitmap.getRenderableTexturePath());
	}
	public static BufferedImage getBlankImage() {
		return BLPHandler.blankImage;
	}


	public GPUReadyTexture loadTexture2(DataSource dataSource, String filepath) {
		String lowerFilePath = filepath.toLowerCase(Locale.US);
		GPUReadyTexture gpuReadyTexture = getCachedGpuReadyTexture(lowerFilePath);
		if (gpuReadyTexture != null) {
			return gpuReadyTexture;
		} else {
			getTexture(dataSource, filepath);

			return getCachedGpuReadyTexture(lowerFilePath);
		}
	}

	private GPUReadyTexture getCachedGpuReadyTexture(String lowerFilePath) {
		if (cache.get(lowerFilePath) != null && cache.get(lowerFilePath).getGpuReadyTexture() != null) {
			return cache.get(lowerFilePath).getGpuReadyTexture();
		} else if (nonCache.get(lowerFilePath) != null && nonCache.get(lowerFilePath).getGpuReadyTexture() != null) {
			return nonCache.get(lowerFilePath).getGpuReadyTexture();
		}
		return null;
	}

	private BufferedImage getTexture(DataSource dataSource, String filepath) {
		try {
			String key = filepath.toLowerCase(Locale.US);
			BufferedImage cachedBufferedImage = getCachedBufferedImage(key);
			if (cachedBufferedImage != null) {
				return cachedBufferedImage;
			} else if (dataSource != null) {
				ImageThingiHelper imageThingi = getNewImageThingiHelper(dataSource, filepath);

				if (imageThingi != null) {
					if (dataSource.allowDownstreamCaching(filepath)) {
						cache.put(key, imageThingi);
					} else {
						nonCache.put(key, imageThingi);
					}
					return imageThingi.getBufferedImage();
				}
			}
			return null;

		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	private BufferedImage getCachedBufferedImage(String filepath) {
		String key = filepath.toLowerCase(Locale.US);
		if (cache.get(key) != null && cache.get(key).getBufferedImage() != null) {
			return cache.get(key).getBufferedImage();
		} else if (nonCache.get(key) != null && nonCache.get(key).getBufferedImage() != null) {
			return nonCache.get(key).getBufferedImage();
		}
		return null;
	}

	private ImageThingiHelper getNewImageThingiHelper(DataSource dataSource, String filepath) throws IOException {
		String ddsFilepath = filepath.replaceAll("(\\.blp$)|(\\.tif$)", ".dds");
		String nameOnly = filepath.replaceAll(".*[/\\\\]", "");

		String[] filePaths = new String[] {ddsFilepath, nameOnly, filepath};

		for (String path : filePaths) {
			BufferedImage resultImage = loadTextureDirectly(dataSource, path);
			if (resultImage != null) {
				return new ImageThingiHelper(dataSource.getFile(path), resultImage);
			}
		}
		return null;
	}

	private BufferedImage loadTextureDirectly(DataSource dataSource, String filepath) throws IOException {
		if (dataSource.has(filepath)) {
			try (final InputStream imageDataStream = dataSource.getResourceAsStream(filepath)) {
				if (imageDataStream != null) {
					if (filepath.toLowerCase(Locale.US).endsWith(".tga")) {
						return TgaFile.readTGA(filepath, imageDataStream);
					} else if (filepath.toLowerCase(Locale.US).endsWith(".blp")) {
						return forceBufferedImagesRGB(ImageIO.read(imageDataStream));
					} else {
						return ImageIO.read(imageDataStream);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Convert an input buffered image into sRGB color space using component values
	 * directly instead of performing a color space conversion.
	 *
	 * @param in Input image to be converted.
	 * @return Resulting sRGB image.
	 */
	private BufferedImage forceBufferedImagesRGB(final BufferedImage in) {
		if (in == null) {
			return null;
		}
		// Resolve input ColorSpace.
		ColorSpace inCS = in.getColorModel().getColorSpace();
		ColorSpace sRGBCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		if (inCS == sRGBCS) {
			// Already is sRGB.
			return in;
		}
		if (inCS.getNumComponents() != sRGBCS.getNumComponents()) {
			throw new IllegalArgumentException("Input color space has different number of components from sRGB.");
		}

		// Draw input.
		ColorModel lRGBModel = new ComponentColorModel(inCS, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		ColorModel sRGBModel = new ComponentColorModel(sRGBCS, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		BufferedImage lRGB = new BufferedImage(lRGBModel, lRGBModel.createCompatibleWritableRaster(in.getWidth(), in.getHeight()), false, null);
		for (int i = 0; i < in.getWidth(); i++) {
			for (int j = 0; j < in.getHeight(); j++) {
				lRGB.setRGB(i, j, in.getRGB(i, j));
			}
		}
		// Convert to sRGB.
		return new BufferedImage(sRGBModel, lRGB.getRaster(), false, null);
	}
}
