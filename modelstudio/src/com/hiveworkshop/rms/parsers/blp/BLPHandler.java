package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.twiImageStuff.TwiTGAFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
	Map<String, TextureHelper> cache = new HashMap<>();
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
	}

	public static BufferedImage getGameTex(String iconTexturePath) {
		return get().getTexture(GameDataFileSystem.getDefault(), iconTexturePath);
	}

	public static BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory) {
		return BLPHandler.get().getTexture(workingDirectory, bitmap);
//		return BLPHandler.get().getTexture(workingDirectory, bitmap.getRenderableTexturePath());
	}
	public static BufferedImage getBlankImage() {
		return BLPHandler.blankImage;
	}

	public TextureHelper getTextureHelper(DataSource dataSource, Bitmap bitmap) {
		String lowerFilePath = bitmap.getRenderableTexturePath().toLowerCase(Locale.US);
		TextureHelper textureHelper = cache.get(lowerFilePath);
		if (textureHelper != null) {
			return textureHelper;
		} else {
			try {
//				cacheBufferedImage(dataSource, bitmap.getRenderableTexturePath());
				return getNewTextureHelper(dataSource, bitmap);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
//			return getCachedGpuReadyTexture(lowerFilePath);
		}
	}

	Map<Color, GPUReadyTexture> colorTextureMap = new HashMap<>();
	public GPUReadyTexture getColorTexture(Color color){
		return colorTextureMap.computeIfAbsent(color, k -> ImageUtils.getGPUColorTexture(color));
	}


	private BufferedImage getTexture(DataSource dataSource, String filepath) {
		BufferedImage cachedBufferedImage = getCachedBufferedImage(filepath);
		if (cachedBufferedImage != null) {
			return cachedBufferedImage;
		} else if (dataSource != null) {
			try {
				return cacheAndGetBufferedImage(dataSource, filepath);
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
		}
		return null;
	}

	private BufferedImage getTexture(DataSource dataSource, Bitmap bitmap) {
		BufferedImage cachedBufferedImage = getCachedBufferedImage(bitmap.getRenderableTexturePath());
		if (cachedBufferedImage != null) {
			return cachedBufferedImage;
		} else if (dataSource != null) {
			try {
				return cacheAndGetBufferedImage(dataSource, bitmap);
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
		}
		return null;
	}

	private GPUReadyTexture getCachedGpuReadyTexture(String lowerFilePath) {
		if (cache.get(lowerFilePath) != null) {
			return cache.get(lowerFilePath).getCashedGpuReadyTexture();
		}
		return null;
	}

	private BufferedImage getCachedBufferedImage(String filepath) {
		String key = filepath.toLowerCase(Locale.US);
		if (cache.get(key) != null) {
			return cache.get(key).getBufferedImage();
		}
		return null;
	}

	private BufferedImage cacheAndGetBufferedImage(DataSource dataSource, String filepath) throws IOException {
		TextureHelper imageThingi = getNewTextureHelper(dataSource, filepath);

		if (imageThingi != null) {
			return imageThingi.getBufferedImage();
		}
		return null;
	}

	private BufferedImage cacheAndGetBufferedImage(DataSource dataSource, Bitmap bitmap) throws IOException {
		TextureHelper imageThingi = getNewTextureHelper(dataSource, bitmap);

		if (imageThingi != null) {
			return imageThingi.getBufferedImage();
		}
		return null;
	}
	private GPUReadyTexture cacheAndGetBPUReadyImage(DataSource dataSource, String filepath) throws IOException {
		TextureHelper imageThingi = getNewTextureHelper(dataSource, filepath);

		if (imageThingi != null) {
			return imageThingi.getCashedGpuReadyTexture();
		}
		return null;
	}
	private GPUReadyTexture cacheAndGetBPUReadyImage(DataSource dataSource, Bitmap bitmap) throws IOException {
		TextureHelper textureHelper = getNewTextureHelper(dataSource, bitmap);

		if (textureHelper != null) {
			return textureHelper.getCashedGpuReadyTexture();
		}
		return null;
	}


	private TextureHelper getNewTextureHelper(DataSource dataSource, String filepath) throws IOException {
		String ddsFilepath = filepath.replaceAll("(\\.blp$)|(\\.tif$)", ".dds");
		String nameOnly = filepath.replaceAll(".*[/\\\\]", "");

		String[] filePaths = new String[] {ddsFilepath, nameOnly, filepath};

		for (String path : filePaths) {
			BufferedImage resultImage = loadTextureFromSource(dataSource, path);
			if (resultImage != null) {
				TextureHelper textureHelper = new TextureHelper(dataSource.getFile(path), resultImage, dataSource.allowDownstreamCaching(filepath));
				cache.put(filepath.toLowerCase(Locale.US), textureHelper);
				return textureHelper;
			}
		}
		if(filepath.toLowerCase().matches("\\w:.+")){
			File textureFile = getTextureFile(filepath);
			BufferedImage bufferedImage = loadTextureFromFile(textureFile);
			TextureHelper textureHelper = new TextureHelper(textureFile, bufferedImage, true);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);
			return textureHelper;
		}
		return null;
	}

	private TextureHelper getNewTextureHelper(DataSource dataSource, Bitmap bitmap) throws IOException {
		String filepath = bitmap.getRenderableTexturePath();
		String ddsFilepath = filepath.replaceAll("(\\.blp$)|(\\.tif$)", ".dds");
		String nameOnly = filepath.replaceAll(".*[/\\\\]", "");

		String[] filePaths = new String[] {ddsFilepath, nameOnly, filepath};

		for (String path : filePaths) {
			BufferedImage resultImage = loadTextureFromSource(dataSource, path);
			if (resultImage != null) {
				TextureHelper textureHelper = new TextureHelper(dataSource.getFile(path), resultImage, dataSource.allowDownstreamCaching(filepath), bitmap);
				cache.put(filepath.toLowerCase(Locale.US), textureHelper);
				return textureHelper;
			}
		}
		if(filepath.toLowerCase().matches("\\w:.+")){
			File textureFile = getTextureFile(filepath);
			BufferedImage bufferedImage = loadTextureFromFile(textureFile);
			TextureHelper textureHelper = new TextureHelper(textureFile, bufferedImage, true, bitmap);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);

			return textureHelper;
		}
		return null;
	}




	private BufferedImage loadTextureFromSource(DataSource dataSource, String filepath) throws IOException {
		if (dataSource.has(filepath)) {
			try (final InputStream imageDataStream = dataSource.getResourceAsStream(filepath)) {
				if (imageDataStream != null) {
					if (filepath.toLowerCase(Locale.US).endsWith(".tga")) {
						return new TwiTGAFile(imageDataStream).getAsBufferedImage();
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

	private File getTextureFile(String filepath) {
		Path path = Paths.get(filepath);
		System.out.println("systemPath! " + filepath + ", path: " + path + " (" + path.getFileName() + ")");
		if(!path.getFileName().toString().equals("")){
			return new File(filepath);
		}
		return null;
	}
	private BufferedImage loadTextureFromFile(File file) throws IOException {
		if(file != null && file.exists()){
			try (final InputStream imageDataStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
				return ImageIO.read(imageDataStream);
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
