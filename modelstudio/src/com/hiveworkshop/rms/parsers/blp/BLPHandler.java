package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
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
	private static TextureLoader currTex;

	public static BLPHandler get() {
		if (current == null) {
			current = new BLPHandler();
		} else if (current.cache.size() > 1056) {
			System.out.println("Dropping texture Cache" + current.cache.size());
			current.dropCache();
		}
		return current;
	}
	public static TextureLoader getTL() {
		if (currTex == null) {
			currTex = new TextureLoader();
		} else if (currTex.cache.size() > 1056) {
			System.out.println("Dropping texture Cache" + currTex.cache.size());
			currTex.cache.dropCache();
		}
		return currTex;
	}

	public void dropCache() {
		cache.clear();
		currTex.cache.dropCache();
	}

	public static BufferedImage getImage(String iconTexturePath) {
		return getTL().getImage(iconTexturePath, GameDataFileSystem.getDefault());
	}

	public static BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory) {
		System.out.println("bitmap: " + bitmap);
		if(bitmap != null){
			System.out.println(bitmap.getPath());
			System.out.println(bitmap.getName());
		}
		return getTL().getImage(bitmap, workingDirectory);
	}
	public static BufferedImage getBlankImage() {
		return BLPHandler.blankImage;
	}

	public TextureHelper getTextureHelper(DataSource dataSource, Bitmap bitmap) {
		return currTex.getTextureHelper(dataSource, bitmap);
	}

	Map<Color, GPUReadyTexture> colorTextureMap = new HashMap<>();
	public GPUReadyTexture getColorTexture(Color color){
		return colorTextureMap.computeIfAbsent(color, k -> ImageUtils.getGPUColorTexture(color));
	}
}
