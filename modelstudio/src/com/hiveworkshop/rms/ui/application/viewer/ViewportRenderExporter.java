package com.hiveworkshop.rms.ui.application.viewer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

public class ViewportRenderExporter {

	public static BufferedImage getBufferedImage(PerspectiveViewport perspectiveViewport) {
		try {
			int height = perspectiveViewport.getHeight();
			int width = perspectiveViewport.getWidth();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			// paintComponent(image.getGraphics(),5);
			Pbuffer buffer = new Pbuffer(width, height, new PixelFormat(8,8,0), null, null);
			buffer.makeCurrent();
			ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);

			perspectiveViewport.initGL();
			perspectiveViewport.paintGL(false);

			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL_UNSIGNED_BYTE, pixels);

			final int[] array = new int[pixels.capacity() / 4];
			final int[] flippedArray = new int[pixels.capacity() / 4];

			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				int rgba = array[i];
				int a = rgba & 0xFF;
				if(rgba != 255 || a != 255){
					System.out.println("rgba: " + rgba + ", (rgba>>>8): " + (rgba >>> 8) +  ", a: " + a + ", (a<<24): " + (a << 24));
				}
				array[i] = (rgba >>> 8) | (a << 24);
			}

			for (int i = 0; i < height; i++) {
				System.arraycopy(array, i * width, flippedArray, (height - 1 - i) * width, width);
			}
			image.getRaster().setDataElements(0, 0, width, height, flippedArray);

			buffer.releaseContext();
			return image;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
