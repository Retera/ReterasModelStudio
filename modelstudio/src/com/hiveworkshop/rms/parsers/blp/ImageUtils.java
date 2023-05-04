package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.util.ImageUtils.GU;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageUtils {
	private static final int BYTES_PER_PIXEL = 4;

	public static GPUReadyTexture getGPUColorTexture(Color color){
		ByteBuffer byteBuffer = getByteBuffer(getColorImage(color));
		return new GPUReadyTexture(byteBuffer, 16, 16);
	}

	public static BufferedImage getColorImage(Color color){
		BufferedImage bufferedImage = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[bufferedImage.getWidth()*bufferedImage.getHeight()];
		Arrays.fill(pixels, color.getRGB());

		bufferedImage.setRGB(0,0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
//		Graphics graphics = bufferedImage.getGraphics();
//		graphics.setColor(color);
////		graphics.drawRect(0,0,16,16);
//		graphics.fillRect(0,0,16,16);
//		graphics.dispose();
		return bufferedImage;
	}
	public static BufferedImage getColorImage(Color color, int size){
		BufferedImage bufferedImage = new BufferedImage(size,size, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(color);
//		graphics.drawRect(0,0,size,size);
		graphics.fillRect(0,0,size,size);
		graphics.dispose();
		return bufferedImage;
	}

	public static BufferedImage removeAlphaChannel(BufferedImage source) {
		BufferedImage combined = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);

		return combined;
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
				buffer.put((byte) ((pixel >>  8) & 0xFF)); // Green component
				buffer.put((byte) ((pixel >>  0) & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip();
		return buffer;
	}
	public static ByteBuffer getByteBufferIsolateChannel(BufferedImage bufferedImage, int channel) {
		int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * BYTES_PER_PIXEL);
		// 4 for RGBA, 3 for RGB

		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				int pixel = pixels[(y * bufferedImage.getWidth()) + x];

				switch (channel){
					case 0 ->  putInBuffer(buffer, pixel >> 16, pixel >> 16, pixel >> 16, 0xFF);
					case 1 ->  putInBuffer(buffer, pixel >> 16, pixel >> 16, pixel >> 16, 0xFF);
					case 2 ->  putInBuffer(buffer, pixel >> 16, pixel >> 16, pixel >> 16, 0xFF);
					case 3 ->  putInBuffer(buffer, pixel >> 16, pixel >> 16, pixel >> 16, 0xFF);
					default -> putInBuffer(buffer, pixel >> 16, pixel >>  8, pixel >>  0, pixel >> 24);
				}

				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >>  8) & 0xFF)); // Green component
				buffer.put((byte) ((pixel >>  0) & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip();
		return buffer;
	}


	public static BufferedImage getBufferedImageIsolateChannel(BufferedImage bufferedImage, ColorMode colorMode) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		WritableRaster sourceRaster = bufferedImage.getRaster();

		BufferedImage channelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster channelImageRaster = channelImage.getRaster();


		float[] srcPixelData = new float[sourceRaster.getNumBands()];
		float[] pixelData = new float[4];
		int[] dataBands = getRGBADataBands(bufferedImage);

		for(int h = 0; h<height; h++){
			for(int w = 0; w<width; w++){
				sourceRaster.getPixel(w, h, srcPixelData);

//				for(int pd = 0; pd< sourceRaster.getNumBands(); pd++){
//					pixelData[pd] = pixelData1[pd]*pixelData2[pd];
//				}
				switch (colorMode){
					case RED_RED ->     fillRasterData(pixelData, srcPixelData[dataBands[0]],                       0.0f,                       0.0f,                     255.0f);
					case GREEN_GREEN -> fillRasterData(pixelData,                       0.0f, srcPixelData[dataBands[1]],                       0.0f,                     255.0f);
					case BLUE_BLUE ->   fillRasterData(pixelData,                       0.0f,                       0.0f, srcPixelData[dataBands[2]],                     255.0f);
					case RGBA ->        fillRasterData(pixelData, srcPixelData[dataBands[0]], srcPixelData[dataBands[1]], srcPixelData[dataBands[2]], srcPixelData[dataBands[3]]);
					case RED_BW ->      fillRasterData(pixelData, srcPixelData[dataBands[0]], srcPixelData[dataBands[0]], srcPixelData[dataBands[0]],                     255.0f);
					case GREEN_BW ->    fillRasterData(pixelData, srcPixelData[dataBands[1]], srcPixelData[dataBands[1]], srcPixelData[dataBands[1]],                     255.0f);
					case BLUE_BW ->     fillRasterData(pixelData, srcPixelData[dataBands[2]], srcPixelData[dataBands[2]], srcPixelData[dataBands[2]],                     255.0f);
					case ALPHA ->       fillRasterData(pixelData, srcPixelData[dataBands[3]], srcPixelData[dataBands[3]], srcPixelData[dataBands[3]],                     255.0f);
					case RGB ->         fillRasterData(pixelData, srcPixelData[dataBands[0]], srcPixelData[dataBands[1]], srcPixelData[dataBands[2]],                     255.0f);
				}
				channelImageRaster.setPixel(w, h, pixelData);
			}
		}
		channelImage.setData(channelImageRaster);

		return channelImage;
	}

	private static int[] getRGBADataBands(BufferedImage bufferedImage) {
		return switch (bufferedImage.getType()){
			case BufferedImage.TYPE_INT_BGR ->  new int[]{2, 1, 0};
			case BufferedImage.TYPE_INT_RGB ->  new int[]{0, 1, 2};
			case BufferedImage.TYPE_INT_ARGB -> new int[]{0, 1, 2, 3};
			default -> new int[]{0, 1, 2, 3};
		};
	}


	private static void fillRasterData(float[] pixelData, float... data){
		for (int i = 0; i<pixelData.length && i< data.length; i++){
			pixelData[i] = data[i];
		}
	}
	public static ByteBuffer getByteBufferIsolateChannel(BufferedImage bufferedImage, ColorMode colorMode) {
		int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
		bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * BYTES_PER_PIXEL);
		// 4 for RGBA, 3 for RGB

		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				int pixel = pixels[(y * bufferedImage.getWidth()) + x];

				switch (colorMode){
					case RED_RED ->     putInBuffer(buffer, pixel >> 16,           0,           0,        0xFF);
					case RED_BW ->      putInBuffer(buffer, pixel >> 16, pixel >> 16, pixel >> 16,        0xFF);
					case GREEN_GREEN -> putInBuffer(buffer,           0, pixel >>  8,           0,        0xFF);
					case GREEN_BW ->    putInBuffer(buffer, pixel >>  8, pixel >>  8, pixel >>  8,        0xFF);
					case BLUE_BLUE ->   putInBuffer(buffer,           0,           0, pixel >>  0,        0xFF);
					case BLUE_BW ->     putInBuffer(buffer, pixel >>  0, pixel >>  0, pixel >>  0,        0xFF);
					case ALPHA ->       putInBuffer(buffer, pixel >> 24, pixel >> 24, pixel >> 24,        0xFF);
					case RGBA ->        putInBuffer(buffer, pixel >> 16, pixel >>  8, pixel >>  0,        0xFF);
					case RGB ->         putInBuffer(buffer, pixel >> 16, pixel >>  8, pixel >>  0, pixel >> 24);
				}
			}
		}

		buffer.flip();
		return buffer;
	}
	private static void putInBuffer(ByteBuffer buffer, int shiftedInt){
		buffer.put((byte) (shiftedInt & 0xFF));
	}
	private static void putInBuffer(ByteBuffer buffer, int... shiftedInts){
		for (int shiftedInt : shiftedInts){
			buffer.put((byte) (shiftedInt & 0xFF));
		}
	}

	public static BufferedImage getCheckerImage(int width, int height, int squareSize, Color color1, Color color2){
		int[] pixels = new int[width*height+squareSize];
//		Arrays.fill(pixels, MathUtils.uint8ToUint32((byte) 255, (byte) 192, (byte) 192, (byte) 192));
		Arrays.fill(pixels, color1.getRGB());
//		int color = MathUtils.uint8ToUint32((byte) 255, (byte) 127, (byte) 127, (byte) 127);
		int color = color2.getRGB();
		for (int h = 0; h < height; h++){
			int hPix = h*width;
			int wStart = squareSize - (h % (squareSize * 2)) <= 0 ? squareSize : 0;
			for (int w = wStart; w < width; w += (squareSize * 2)){
				Arrays.fill(pixels, hPix + w, hPix + w + squareSize, color);
			}
		}

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		bufferedImage.setRGB(0,0, width, height, pixels, 0, width);
		return bufferedImage;
	}

	public static BufferedImage getXImage(int imageSize, int squareSize, Color color){
		final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setColor(color);
		GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, squareSize);
		int dist1 = (imageSize - squareSize)/2;
		int dist2 = imageSize-dist1;
		GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
		return image;
	}

	public enum ColorMode {
		RGBA,
		RGB,
		RED_BW,
		GREEN_BW,
		BLUE_BW,
		ALPHA,
		RED_RED,
		GREEN_GREEN,
		BLUE_BLUE,
	}
}
