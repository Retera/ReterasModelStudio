package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.twiImageStuff.TwiTGAFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import static com.hiveworkshop.rms.util.ImageUtils.ImageCreator.forceBufferedImagesRGB;

public class TextureLoader {
	TextureCache cache = new TextureCache();
	Bitmap tempBitmap = new Bitmap();
	BufferedImage checkerImage = ImageUtils.getCheckerImage(64, 64, 2, new Color(200, 80, 200, 255), new Color(110, 0, 110, 255));
	BufferedImage normalPlaceholder = ImageUtils.getColorImage(new Color(127, 127, 0, 255));
	BufferedImage ormPlaceholder = ImageUtils.getColorImage(new Color(127, 85, 0, 0));

	public BufferedImage getImage(String path) {
		return getTexture(GameDataFileSystem.getDefault(), tempBitmap.setPath(path));
	}
	public BufferedImage getImage(String path, DataSource workingDirectory) {
		return getTexture(workingDirectory, tempBitmap.setPath(path));
	}

	public BufferedImage getImage(Bitmap bitmap) {
		return getTexture(GameDataFileSystem.getDefault(), bitmap);
	}
	public BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory) {
		return getTexture(workingDirectory, bitmap);
	}

	public boolean isBitmapFound(Bitmap bitmap) {
		TextureHelper textureHelper = cache.get(bitmap.getRenderableTexturePath());
		if (textureHelper != null) {
			BufferedImage bufferedImage = textureHelper.getBufferedImage();
			return bufferedImage != checkerImage && bufferedImage != normalPlaceholder && bufferedImage != ormPlaceholder;
		}
		return false;
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

	private BufferedImage getCachedBufferedImage(String filepath) {
		String key = filepath.toLowerCase(Locale.US);
		if (cache.get(key) != null) {
			return cache.get(key).getBufferedImage();
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


	public TextureHelper getTextureHelper(DataSource dataSource, Bitmap bitmap){
		String lowerFilePath = bitmap.getRenderableTexturePath().toLowerCase(Locale.US);
		TextureHelper textureHelper = cache.get(lowerFilePath);
		if (textureHelper != null) {
			return textureHelper;
		} else {
			try {
				return getNewTextureHelper(dataSource, bitmap);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private TextureHelper getNewTextureHelper(DataSource dataSource, Bitmap bitmap) throws IOException {
		String filepath = bitmap.getRenderableTexturePath();
		String ddsFilepath = filepath.replaceAll("(\\.blp$)|(\\.tif$)", ".dds");
		String blpFilepath = filepath.replaceAll("(\\.dds$)|(\\.tif$)", ".blp");
		String nameOnly = filepath.replaceAll(".*[/\\\\]", "");
		String ddsNameOnly = ddsFilepath.replaceAll(".*[/\\\\]", "");
		String blpNameOnly = blpFilepath.replaceAll(".*[/\\\\]", "");

		String[] filePaths = new String[] {ddsFilepath, nameOnly, ddsNameOnly, blpNameOnly, filepath, blpFilepath};
//		System.out.println("getNewTextureHelper for filepath: \"" + filepath + "\"");

		for (String path : filePaths) {
			if (dataSource.has(path)){
				File file = dataSource.getFile(path);
				BufferedImage resultImage = loadTextureFromFile(file);
				if (resultImage != null) {
//				System.out.println("found image with path: \"" + path + "\"");
					TextureHelper textureHelper = new TextureHelper(file, resultImage, !dataSource.allowDownstreamCaching(filepath), bitmap);
					cache.put(filepath.toLowerCase(Locale.US), textureHelper);
					return textureHelper;
				}
			}
		}

		if(filepath.toLowerCase().matches("\\w:.+")){
			File textureFile = getTextureFile(filepath);
//			System.out.println("loading from disc: \"" + filepath + "\"");
			BufferedImage bufferedImage = loadTextureFromFile(textureFile);
			TextureHelper textureHelper = new TextureHelper(textureFile, bufferedImage, true, bitmap);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);

			return textureHelper;
		} else if (filepath.toLowerCase().matches("(.+[\\\\/])*(.+_)?orm\\.\\w{3,4}")){
//			System.out.println("could not find ORM: \"" + filepath + "\"");

			TextureHelper textureHelper = new TextureHelper(null, ormPlaceholder, false, bitmap);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);
		} else if (filepath.toLowerCase().matches("(.+[\\\\/])*(.+_)?normal\\.\\w{3,4}")){
//			System.out.println("could not find Normal: \"" + filepath + "\"");
			BufferedImage bufferedImage = normalPlaceholder;
			TextureHelper textureHelper = new TextureHelper(null, bufferedImage, false, bitmap);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);
		} else {
//			System.out.println("could not find texture: \"" + filepath + "\"");
			BufferedImage bufferedImage = checkerImage;
			TextureHelper textureHelper = new TextureHelper(null, bufferedImage, false, bitmap);
			cache.put(filepath.toLowerCase(Locale.US), textureHelper);
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

	private BufferedImage loadTextureFromFile1(File file) throws IOException {
		if(file != null && file.exists()){
			try (final InputStream imageDataStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
				return ImageIO.read(imageDataStream);
			}
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
	private BufferedImage loadTextureFromFile(File file) throws IOException {
		if (file != null && file.exists()) {
			if (file.getPath().toLowerCase(Locale.US).endsWith(".tga")) {
				try (InputStream inputStream = new FileInputStream(file)){
					return new TwiTGAFile(inputStream).getAsBufferedImage();
				}
			} else if (file.getPath().toLowerCase(Locale.US).endsWith(".blp")) {
				return forceBufferedImagesRGB(ImageIO.read(file));
			} else {
				return ImageIO.read(file);
			}
		}

		return null;
	}
}
