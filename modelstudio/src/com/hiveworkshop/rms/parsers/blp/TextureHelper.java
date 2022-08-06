package com.hiveworkshop.rms.parsers.blp;


import com.hiveworkshop.rms.editor.model.Bitmap;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class TextureHelper {
	private static final int BYTES_PER_PIXEL = 4;
	private BufferedImage bufferedImage;
	private GPUReadyTexture gpuReadyTexture = null;
	private ByteBuffer buffer;
	private int width;
	private int height;
	private Bitmap bitmap;
	private File file;
	private Long lastModified = 0L;
	private String path = "";

	public TextureHelper(File file, BufferedImage bufferedImage) {
		this.file = file;
		this.bufferedImage = bufferedImage;
		if (file != null && file.exists()) {
			this.lastModified = file.lastModified();
			this.path = file.getPath();
		}
	}

	public TextureHelper(File file, BufferedImage bufferedImage, boolean shouldCash) {
		this.bufferedImage = bufferedImage;
		if (!shouldCash) {
			this.file = file;
			if (file != null && file.exists()) {
				this.lastModified = file.lastModified();
				this.path = file.getPath();
			}
		}
	}

	public TextureHelper(File file, BufferedImage bufferedImage, boolean shouldCash, Bitmap bitmap) {
		this.bufferedImage = bufferedImage;
		this.bitmap = bitmap;
		if (!shouldCash) {
			this.file = file;
			if (file != null && file.exists()) {
				this.lastModified = file.lastModified();
				this.path = file.getPath();
			}
		}
	}

	public boolean hasBeenModified() {
		return file != null && (!file.exists() || new File(path).lastModified() != lastModified);
	}
//	public boolean hasBeenModified() {
//		return file == null || !file.exists() || new File(path).lastModified() != lastModified;
//	}

	public BufferedImage getBufferedImage() {
		if (!hasBeenModified()) {
			return bufferedImage;
		}
		return null;
	}

	public BufferedImage getCashedBufferedImage() {
		return bufferedImage;
	}

	public GPUReadyTexture getGpuReadyTexture() {
		if (bufferedImage != null && !hasBeenModified()) {
			if (gpuReadyTexture == null) {
				buffer = getByteBuffer(bufferedImage);
				width = bufferedImage.getWidth();
				height = bufferedImage.getHeight();
				gpuReadyTexture = new GPUReadyTexture(buffer, width, height);
			}
			return gpuReadyTexture;
		}
		return null;
	}

	public ByteBuffer getBuffer() {
		if(buffer == null){
			buffer = getByteBuffer(bufferedImage);
			width = bufferedImage.getWidth();
			height = bufferedImage.getHeight();
		}
		return buffer;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isWrapWidth() {
		return bitmap != null && bitmap.isWrapWidth();
	}
	public boolean isWrapHeight() {
		return bitmap != null && bitmap.isWrapHeight();
	}
	//	public GPUReadyTexture getGpuReadyTexture() {
////		if (bufferedImage != null && !hasBeenModified()) {
////			if (gpuReadyTexture == null) {
////				gpuReadyTexture = new GPUReadyTexture(getByteBuffer(bufferedImage), bufferedImage.getWidth(), bufferedImage.getHeight());
////			}
////			return gpuReadyTexture;
////		}
////		return null;
//		return getGpuReadyTexture(false);
//	}

	public GPUReadyTexture getCashedGpuReadyTexture() {
//		if (bufferedImage != null) {
//			if (gpuReadyTexture == null) {
//				gpuReadyTexture = new GPUReadyTexture(getByteBuffer(bufferedImage), bufferedImage.getWidth(), bufferedImage.getHeight());
//			}
//			return gpuReadyTexture;
//		}
//		return null;
		return getGpuReadyTexture(true);
	}

	private GPUReadyTexture getGpuReadyTexture(boolean skipCheckModified) {
		if (bufferedImage != null && (skipCheckModified || !hasBeenModified())) {
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
