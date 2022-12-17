package com.hiveworkshop.rms.parsers.blp;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TextureCache {
	private static Map<String, TextureHelper> cache = new HashMap<>();
	private static final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	static {
		((DataBufferInt) blankImage.getRaster().getDataBuffer()).getData()[0] = 16777215;
	}

	public TextureCache dropCache() {
		cache.clear();
		return this;
	}

	public TextureHelper get(String filepath){
		String key = filepath.toLowerCase(Locale.US);
		return cache.get(key);
	}

	public TextureCache put(String filepath, TextureHelper textureHelper){
		String key = filepath.toLowerCase(Locale.US);
		cache.put(key, textureHelper);
		return this;
	}
}
