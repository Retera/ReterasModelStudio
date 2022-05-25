package com.hiveworkshop.rms.parsers.blp;

import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ImageUtils {
	private static final int BYTES_PER_PIXEL = 4;

	public static GPUReadyTexture getGPUColorTexture(Color color){
		ByteBuffer byteBuffer = getByteBuffer(getColorImage(color));
		return new GPUReadyTexture(byteBuffer, 16, 16);
	}

	public static BufferedImage getColorImage(Color color){
		BufferedImage bufferedImage = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(color);
		graphics.drawRect(0,0,16,16);
		graphics.dispose();
		return bufferedImage;
	}


	public static ByteBuffer getByteBuffer(BufferedImage bufferedImage) {
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
