package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.parsers.twiImageStuff.TwiWriteGif;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class SplatImageGenerator {

	public static ImageIcon generateIcon(SplatMappings.Splat splat) {
		float timePerFrame = .1f;

		int lsFrSpan = 1 + splat.getuVLifespanEnd() - splat.getuVLifespanStart();
		int dcFrSpan = 1 + splat.getuVDecayEnd() - splat.getuVDecayStart();
		int numLsImgs = lsFrSpan * splat.getLifespanRepeat();
		int numDcImgs = dcFrSpan * splat.getDecayRepeat();

		int minNumLsFrames = lsFrSpan * splat.getLifespanRepeat();
		int minNumDcFrames = dcFrSpan * splat.getDecayRepeat();
		int lsColorChange = (int) (getMaxChannelDiff(splat.getMiddleRGBA(), splat.getStartRGBA()) / 4);
		int dcColorChange = (int) (getMaxChannelDiff(splat.getEndRGBA(), splat.getMiddleRGBA()) / 4);
		int numLsFrames = Math.max(minNumLsFrames, Math.min(lsColorChange, (int) (splat.getLifespan() / timePerFrame)));
		int numDcFrames = Math.max(minNumDcFrames, Math.min(dcColorChange, (int) (splat.getDecay() / timePerFrame)));

		float timePerLsFrame = splat.getLifespan() / numLsFrames;
		float timePerDcFrame = splat.getDecay() / numDcFrames;

		int numFrames = numLsFrames + numDcFrames;
		BufferedImage[] frames = new BufferedImage[numFrames];
		int[] delayTimes = new int[numFrames];

		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		Vec4 colorS = new Vec4(splat.getStartRGBA()).scale(1f / 255f);
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f / 255f);
		Vec4 colorE = new Vec4(splat.getEndRGBA()).scale(1f / 255f);
		Vec4 tempCol = new Vec4();

		int cols = splat.getColumns();
		int width = image.getWidth() / cols;
		int rows = splat.getRows();
		int height = image.getHeight() / rows;

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);
//		System.out.println("numLsFrames:   " + numLsFrames + ",   numDcFrames:   " + numDcFrames);
//		System.out.println("lsColorChange: " + lsColorChange + ", dcColorChange: " + dcColorChange);

		for (int i = 0; i < numFrames; i++) {
			int imgI;
			if (i < numLsFrames) {
				float t = i * timePerLsFrame;
				delayTimes[i] = (int)(timePerLsFrame * 100);
				float timeF = t / splat.getLifespan();
				imgI = ((int) (numLsImgs * timeF)) % lsFrSpan + splat.getuVLifespanStart();
				tempCol.set(colorS).lerp(colorM, timeF);
			} else {
				float t = (i - numLsFrames) * timePerDcFrame;
				delayTimes[i] = (int) (timePerDcFrame * 100);
				float timeF = t / splat.getDecay();
				imgI = ((int) (numDcImgs * timeF)) % dcFrSpan + splat.getuVDecayStart();
				tempCol.set(colorM).lerp(colorE, timeF);
			}

			int xOff = (imgI % rows) * width;
			int yOff = (imgI / rows) * height;
			frames[i] = getScaledTintedSubImage(image, tempCol, xOff, yOff, width, height, 512);
		}

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);

//		System.out.println("generated gifIcon, w: " + width + ", h: " + height);
//		System.out.println("generated gifIcon: " + imageIcon);
		return getImageIcon(frames, delayTimes);
	}
	public static ImageIcon getMidImage(SplatMappings.Splat splat) {

		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f / 255f);

		int cols = splat.getColumns();
		int width = image.getWidth() / cols;
		int rows = splat.getRows();
		int height = image.getHeight() / rows;

		int[] delayTimes = new int[] {1000};
		int imgI = splat.getuVDecayStart();

		int xOff = (imgI % rows) * width;
		int yOff = (imgI / rows) * height;
		image = getScaledSubImage(image, xOff, yOff, width, height, 512);

		Vec4 bgColor = new Vec4(80, 80, 80, 255);
		BufferedImage[] frames = new BufferedImage[] {getTintedImageNoAlpha(image, image, colorM, bgColor)};

		return getImageIcon(frames, delayTimes);
	}


	public static ImageIcon generateIcon(UberSplatMappings.UberSplat splat) {
//		TimeLogger timeLogger = new TimeLogger();
//		timeLogger.start();

		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		if (image == null) {
			image = ImageUtils.getColorImage(new Color(128, 128, 128), 32);
		}
		Vec4 colorS = new Vec4(splat.getStartRGBA()).scale(1f / 255f);
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f / 255f);
		Vec4 colorE = new Vec4(splat.getEndRGBA()).scale(1f / 255f);

		int cols = 1;
		int width = image.getWidth() / cols;
		int rows = 1;
		int height = image.getHeight() / rows;
		image = getScaledSubImage(image, 0, 0, width, height, 512);
//		timeLogger.log("setup1");

		float timePerFrame = .1f;
		int birthColorChange = (int) (getMaxChannelDiff(splat.getMiddleRGBA(), splat.getStartRGBA()) / 4);
		int decayColorChange = (int) (getMaxChannelDiff(splat.getEndRGBA(), splat.getMiddleRGBA()) / 4);
		int numBirthFrames = Math.max(1, Math.min(birthColorChange, (int) (splat.getBirthTime() / timePerFrame)));
		int numPauseFrames = 1;
		int numDecayFrames = Math.max(1, Math.min(decayColorChange, (int) (splat.getDecay() / timePerFrame)));
		int numFrames = numBirthFrames + numPauseFrames + numDecayFrames;


		float timePerBirthFrame = splat.getBirthTime() / numBirthFrames;
		float timePerPauseFrame = splat.getPauseTime() / numPauseFrames;
		float timePerDecayFrame = splat.getDecay() / numDecayFrames;

		BufferedImage[] frames = new BufferedImage[numFrames];
		Vec4 tempCol = new Vec4();
		float bfStep = (1f / numBirthFrames);
		float dcStep = (1f / numDecayFrames);

		int[] delayTimes = new int[numFrames];
		Arrays.fill(delayTimes, 0, numBirthFrames, (int) (timePerBirthFrame * 100));
		Arrays.fill(delayTimes, numBirthFrames, numBirthFrames+numPauseFrames, (int) (timePerPauseFrame * 100));
		Arrays.fill(delayTimes, numBirthFrames+numPauseFrames, numFrames, (int) (timePerDecayFrame * 100));

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);
//		timeLogger.log("frames=" + numFrames);


		float timeF = 0f;
		Vec4 bgColor = new Vec4(80, 80, 80, 255);
		for (int i = 0; i < numFrames; i++) {
			if (i < numBirthFrames) {
				timeF += bfStep;
				tempCol.set(colorS).lerp(colorM, timeF);
				frames[i] = getTintedImageNoAlpha(image, null, tempCol, bgColor);
			} else if (i == numBirthFrames) {
				timeF = 0f;
				tempCol.set(colorM);
				frames[i] = getTintedImageNoAlpha(image, null, tempCol, bgColor);
			} else {
				timeF += dcStep;
				tempCol.set(colorM).lerp(colorE, timeF);
				frames[i] = getTintedImageNoAlpha(image, null, tempCol, bgColor);
			}
		}
//		timeLogger.log("frames created");
		//		ImageIcon imageIcon = getImageIcon(frames, delayTimes);
//		timeLogger.log("imageIcon");
//		timeLogger.print();
//		timeLogger.printTimeFromStart();
		return getImageIcon(frames, delayTimes);
	}
	private static BufferedImage getScaledSubImage(BufferedImage image, int xOff, int yOff, int width, int height, int maxDim) {
		BufferedImage tintedSubImage = getSubImage(image, xOff, yOff, width, height);
		if (maxDim < width || maxDim < height) {
			int w = Math.min(width, maxDim);
			int h = Math.min(height, maxDim);
			float wF = width / (float) w;
			float hF = height / (float) h;
			if (wF < hF) {
				w = (int) (width / hF);
			} else if (hF < wF) {
				h = (int) (height / wF);
			}
			return ImageCreator.getScaledImage(tintedSubImage, w, h);
		}
		return tintedSubImage;
	}
	private static BufferedImage getScaledTintedSubImage(BufferedImage image, Vec4 color, int xOff, int yOff, int width, int height, int maxDim) {
		BufferedImage tintedSubImage = getTintedSubImage(image, color, xOff, yOff, width, height);
		if (maxDim < width || maxDim < height) {
			int w = Math.min(width, maxDim);
			int h = Math.min(height, maxDim);
			float wF = width / (float) w;
			float hF = height / (float) h;
			if (wF < hF) {
				w = (int) (width / hF);
			} else if (hF < wF) {
				h = (int) (height / wF);
			}
			return ImageCreator.getScaledImage(tintedSubImage, w, h);
		}
		return tintedSubImage;
	}
	public static ImageIcon getMidImage(UberSplatMappings.UberSplat splat) {
		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		if (image == null) {
			image = ImageUtils.getColorImage(new Color(128, 128, 128), 32);
		}
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f / 255f);

		int cols = 1;
		int width = image.getWidth() / cols;
		int rows = 1;
		int height = image.getHeight() / rows;
		image = getScaledSubImage(image, 0, 0, width, height, 512);

		int[] delayTimes = new int[] {1000};

		BufferedImage[] frames = new BufferedImage[] {getTintedImageNoAlpha(image, null, colorM, new Vec4(80, 80, 80, 255))};
		return getImageIcon(frames, delayTimes);
	}

	private static float getMaxChannelDiff(Vec4 c1, Vec4 c2) {
		return Math.max(
				Math.max(Math.abs(c1.x - c2.x), Math.abs(c1.y - c2.y)),
				Math.max(Math.abs(c1.z - c2.z), Math.abs(c1.w - c2.w))
		);
	}

	public static ImageIcon getImageIcon(BufferedImage[] frames, int[] delayTimes) {
		byte[] asBytes = TwiWriteGif.getAsBytes(frames, delayTimes);
		if (asBytes != null) {
			return new ImageIcon(asBytes);
		} else {
			return new ImageIcon(ImageUtils.getColorImage(Color.red, 32));
		}
	}

	public static BufferedImage getTintedImage(BufferedImage image, Vec4 color) {
		try {
			int width = image.getWidth();
			int height = image.getHeight();
			final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = newImage.getRaster();

			WritableRaster sourceRaster = image.getRaster();

			float[] srcPixelData = new float[sourceRaster.getNumBands()];
			float[] pixelData = new float[4];

			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					sourceRaster.getPixel(w, h, srcPixelData);
					pixelData[0] = srcPixelData[0] * color.x;
					pixelData[1] = srcPixelData[1] * color.y;
					pixelData[2] = srcPixelData[2] * color.z;
					pixelData[3] = srcPixelData[3] * color.w;
					raster.setPixel(w, h, pixelData);
				}
			}
			newImage.setData(raster);

			return newImage;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getTintedSubImage(BufferedImage image, Vec4 color, int xOff, int yOff, int width, int height) {
		try {
			Vec4 bgColor = new Vec4(80, 80, 80, 255);
			final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = newImage.getRaster();

			WritableRaster sourceRaster = image.getRaster();

			float[] srcPixelData = new float[sourceRaster.getNumBands()];
			float[] pixelData = new float[4];

			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					sourceRaster.getPixel(w + xOff, h + yOff, srcPixelData);
					float alphaMul = srcPixelData[3] / 255f * color.w;
					pixelData[0] = srcPixelData[0] * color.x * alphaMul + (1 - alphaMul) * bgColor.x;
					pixelData[1] = srcPixelData[1] * color.y * alphaMul + (1 - alphaMul) * bgColor.y;
					pixelData[2] = srcPixelData[2] * color.z * alphaMul + (1 - alphaMul) * bgColor.z;
					pixelData[3] = 255f;
//					pixelData[3] = 255f * alphaMul + (1 - alphaMul) * bgColor.w;
					raster.setPixel(w, h, pixelData);
				}
			}
			newImage.setData(raster);

			return newImage;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getTintedImageNoAlpha(BufferedImage image, BufferedImage dest, Vec4 color, Vec4 bgColor) {
		try {
			int width = image.getWidth();
			int height = image.getHeight();
			final BufferedImage newImage = dest == null ? new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) : dest;
			WritableRaster raster = newImage.getRaster();

			WritableRaster sourceRaster = image.getRaster();

			float[] srcPixelData = new float[sourceRaster.getNumBands()];
			float[] pixelData = new float[4];

			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					sourceRaster.getPixel(w, h, srcPixelData);

					float alphaMul = srcPixelData[3] / 255f * color.w;
					pixelData[0] = srcPixelData[0] * color.x * alphaMul + (1 - alphaMul) * bgColor.x;
					pixelData[1] = srcPixelData[1] * color.y * alphaMul + (1 - alphaMul) * bgColor.y;
					pixelData[2] = srcPixelData[2] * color.z * alphaMul + (1 - alphaMul) * bgColor.z;
					pixelData[3] = 255f;
					raster.setPixel(w, h, pixelData);
				}
			}
			newImage.setData(raster);

			return newImage;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getSubImage(BufferedImage image, int xOff, int yOff, int width, int height) {
		try {
			final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			WritableRaster raster = newImage.getRaster();

			WritableRaster sourceRaster = image.getRaster();

			float[] srcPixelData = new float[sourceRaster.getNumBands()];
			float[] pixelData = new float[4];

			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					sourceRaster.getPixel(w + xOff, h + yOff, srcPixelData);
					pixelData[0] = srcPixelData[0];
					pixelData[1] = srcPixelData[1];
					pixelData[2] = srcPixelData[2];
					pixelData[3] = srcPixelData[3];
					raster.setPixel(w, h, pixelData);
				}
			}
			newImage.setData(raster);

			return newImage;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage getMarkSubImages(BufferedImage image, int rows, int cols, int lStart, int lEnd, int dStart, int dEnd) {
		try {
			final BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);


			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					newImage.setRGB(i, j, image.getRGB(i, j));
				}
			}

			int width = image.getWidth() / cols;
			int height = image.getHeight() / rows;

			Graphics graphics = newImage.getGraphics();
			graphics.setColor(Color.CYAN.darker());
			for (int i = lStart; i <= lEnd; i++) {
				int xi = i % rows;
				int x1 = xi * width;
				int yi = i / rows;
				int y1 = yi * height;
				int x2 = (xi + 1) * width - 3;
				int y2 = (yi + 1) * height - 3;
				graphics.drawLine(x1, y1, x2, y1);
				graphics.drawLine(x1, y2, x2, y2);
				graphics.drawLine(x1, y1, x1, y2);
				graphics.drawLine(x2, y1, x2, y2);
			}

			graphics.setColor(Color.BLUE);
			for (int i = dStart; i <= dEnd; i++) {
				int xi = i % rows;
				int x1 = xi * width + 2;
				int yi = i / rows;
				int y1 = yi * height + 2;
				int x2 = (xi + 1) * width - 1;
				int y2 = (yi + 1) * height - 1;
				graphics.drawLine(x1, y1, x2, y1);
				graphics.drawLine(x1, y2, x2, y2);
				graphics.drawLine(x1, y1, x1, y2);
				graphics.drawLine(x2, y1, x2, y2);
			}
			graphics.dispose();

			return newImage;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
