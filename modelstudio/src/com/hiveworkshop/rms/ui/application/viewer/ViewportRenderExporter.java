package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.AnimatedPerspectiveViewport;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

public class ViewportRenderExporter {

	public static BufferedImage getBufferedImage(PerspectiveViewport perspectiveViewport) {
		try {
			int height = perspectiveViewport.getHeight();
			int width = perspectiveViewport.getWidth();

			Pbuffer buffer = new Pbuffer(width, height, new PixelFormat(8,8,0), null, null);
			buffer.makeCurrent();
			ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);

			perspectiveViewport.initGL();
			perspectiveViewport.paintGL(false);

			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL_UNSIGNED_BYTE, pixels);
			BufferedImage image = getBufferedImage(pixels, height, width);
			buffer.releaseContext();
			return image;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getBufferedImage(AnimatedPerspectiveViewport perspectiveViewport) {
		try {
			perspectiveViewport.initGL();
			ByteBuffer pixels = perspectiveViewport.paintGL2();

			int height = perspectiveViewport.getHeight();
			int width = perspectiveViewport.getWidth();

			return getBufferedImage(pixels, height, width);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getBufferedImage(ByteBuffer pixels, int height, int width) {
		final int[] array = new int[pixels.capacity() / 4];
		final int[] flippedArray = new int[pixels.capacity() / 4];

		pixels.asIntBuffer().get(array);
		for (int i = 0; i < array.length; i++) {
			int rgba = array[i];
			int a = rgba & 0xFF;
//			if(rgba != 255 || a != 255){
//				System.out.println("rgba: " + rgba + ", (rgba>>>8): " + (rgba >>> 8) +  ", a: " + a + ", (a<<24): " + (a << 24));
//			}
			array[i] = (rgba >>> 8) | (a << 24);
		}

		for (int i = 0; i < height; i++) {
			System.arraycopy(array, i * width, flippedArray, (height - 1 - i) * width, width);
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.getRaster().setDataElements(0, 0, width, height, flippedArray);
		return image;
	}

	public static BufferedImage getImage(int height, int width, int alphaCutoff, ByteBuffer pixels) {
		final int[] array = new int[pixels.capacity() / 4];
		final int[] flippedArray = new int[pixels.capacity() / 4];

		pixels.asIntBuffer().get(array);
		for (int i = 0; i < array.length; i++) {
			int rgba = array[i];
			int a = rgba & 0xFF;
			a = a < alphaCutoff ? 0 : 255;
			array[i] = (rgba >>> 8) | (a << 24);
		}

		for (int i = 0; i < height; i++) {
			System.arraycopy(array, i * width, flippedArray, (height - 1 - i) * width, width);
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.getRaster().setDataElements(0, 0, width, height, flippedArray);
		return image;
	}

	public static BufferedImage[] getBufferedImagesForGif(ByteBuffer[] byteBuffers, int height, int width, int alphaCutoff) {
		try {
//			Map<Integer, Integer> colorMapCounter = getColorMapCounter(byteBuffers);
//			System.out.println("has " + colorMapCounter.size() + " colors");


			BufferedImage[] images = new BufferedImage[byteBuffers.length];
			for(int bufI= 0; bufI < byteBuffers.length; bufI++){
				images[bufI] = getImage(height, width, alphaCutoff, byteBuffers[bufI]);
			}

			return images;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<Integer, Integer> getColorMapCounter(ByteBuffer[] byteBuffers) {
		Map<Integer, Integer> colorMapCounter = new HashMap<>();
		for(int bufI = 0; bufI < byteBuffers.length; bufI++){
			ByteBuffer pixels = byteBuffers[bufI];

			final int[] array = new int[pixels.capacity() / 4];

			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				int rgba = array[i];
//					int a = 255;
//					rgba = (rgba >>> 8) | (a << 24);
				short[] shorts = ReaderUtils.shortArrayOf(rgba, 8, 4);
				int rgbKey = ReaderUtils.intFromBytes((byte) (shorts[0]/16), (byte) (shorts[1]/16), (byte) (shorts[2]/16));
				int count = colorMapCounter.getOrDefault(rgbKey, 0);
				colorMapCounter.put(rgbKey, count+1);
			}

		}
		return colorMapCounter;
	}
}
