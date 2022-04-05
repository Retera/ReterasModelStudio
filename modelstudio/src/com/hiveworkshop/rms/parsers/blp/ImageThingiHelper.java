package com.hiveworkshop.rms.parsers.blp;


import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class ImageThingiHelper {
	private static final int BYTES_PER_PIXEL = 4;
	BufferedImage bufferedImage;
	GPUReadyTexture gpuReadyTexture = null;
	File file;
	Long lastModified = 0L;
	String path = "";

	public ImageThingiHelper(File file, BufferedImage bufferedImage) {
		this.file = file;
		this.bufferedImage = bufferedImage;
		if (file != null && file.exists()) {
			this.lastModified = file.lastModified();
			this.path = file.getPath();
		}
	}

	public boolean hasBeenModified() {
		if(path.equals("C:\\Users\\twilac\\AppData\\Local\\Temp\\MatrixEaterExtract\\war3.w3mod\\replaceabletextures\\commandbuttons\\btnpeasant.blp")
				&& (file == null || !file.exists() || new File(path).lastModified() != lastModified)){

			new Exception().printStackTrace();
		}
		return file == null || !file.exists() || new File(path).lastModified() != lastModified;
	}

	public BufferedImage getBufferedImage() {
		if (!hasBeenModified()) {
			return bufferedImage;
		}
		return null;
	}

	public GPUReadyTexture getGpuReadyTexture() {
		if (bufferedImage != null && !hasBeenModified()) {
			if (gpuReadyTexture == null) {
				gpuReadyTexture = new GPUReadyTexture(getByteBuffer(bufferedImage), bufferedImage.getWidth(), bufferedImage.getHeight());
			}
			return gpuReadyTexture;
		}
		return null;
	}

	private ByteBuffer getByteBuffer(BufferedImage bufferedImage) {
		int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * BYTES_PER_PIXEL);
		// 4 for RGBA, 3 for RGB

		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				int pixel = pixels[(y * bufferedImage.getWidth()) + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip();
		return buffer;
	}
}
