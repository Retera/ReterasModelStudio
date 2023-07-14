package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.parsers.twiImageStuff.TwiWriteGif;
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class SplatImageGenerator {

	public static ImageIcon generateIcon(SplatMappings.Splat splat){

		int lsFrSpan = 1 + splat.getuVLifespanEnd() - splat.getuVLifespanStart();
		int dcFrSpan = 1 + splat.getuVDecayEnd() - splat.getuVDecayStart();
		int numLsFrames = lsFrSpan * splat.getLifespanRepeat();
		int numDcFrames = dcFrSpan * splat.getDecayRepeat();
		int numFrames = numLsFrames + numDcFrames;
		BufferedImage[] frames = new BufferedImage[numFrames];
		int[] delayTimes = new int[numFrames];

		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		Vec4 colorS = new Vec4(splat.getStartRGBA()).scale(1f/255f);
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f/255f);
		Vec4 colorE = new Vec4(splat.getEndRGBA()).scale(1f/255f);
//		Vec4 colorS = new Vec4(0, 1f,0,0);
//		Vec4 colorM = new Vec4(0,0,1f, 0);
//		Vec4 colorE = new Vec4(1f,1f,0, 0);
		Vec4 tempCol = new Vec4();


		int cols = splat.getColumns();
		int width = image.getWidth() / cols;
		int rows = splat.getRows();
		int height = image.getHeight() / rows;
//		int xOff = (splat.getuVLifespanEnd() % splat.getRows()) * width;
//		int yOff = ((int)(splat.getuVLifespanEnd()/splat.getRows())) * height;

		for (int i = 0; i < numLsFrames; i++){
			tempCol.set(colorS).lerp(colorM, i / (float)numLsFrames);
			tempCol.w = 1f;
			int frameI = i % lsFrSpan + splat.getuVLifespanStart();
			int xOff = (frameI % rows) * width;
			int yOff = (frameI / rows) * height;
			frames[i] = getTintedSubImage(image, tempCol, xOff, yOff, width, height);

		}

		for (int i = numLsFrames; i < numFrames; i++){
			tempCol.set(colorM).lerp(colorE, (i-numLsFrames) / (float)numDcFrames);
			tempCol.w = 1f;
			int frameI = i % dcFrSpan + splat.getuVDecayStart();
			int xOff = (frameI % rows) * width;
			int yOff = (frameI / rows) * height;
			frames[i] = getTintedSubImage(image, tempCol, xOff, yOff, width, height);
		}


		float lsFrTime = splat.getLifespan()/numLsFrames * 100f;
		for (int i = 0; i < numLsFrames; i++){
			delayTimes[i] = (int) lsFrTime;
		}
		float dcFrTime = splat.getDecay()/numDcFrames * 100f;
		for (int i = numLsFrames; i < numFrames; i++){
			System.out.println("delayTime: " + delayTimes[i]);
			delayTimes[i] = (int) dcFrTime;
		}

		ImageIcon imageIcon = getImageIcon(frames, delayTimes);
		System.out.println("generated gifIcon, w: " + width + ", h: " + height);
		System.out.println("generated gifIcon: " + imageIcon);
		return imageIcon;
	}
	public static ImageIcon generateIcon2(SplatMappings.Splat splat){
		float timePerFrame = .1f;

		int lsFrSpan = 1 + splat.getuVLifespanEnd() - splat.getuVLifespanStart();
		int dcFrSpan = 1 + splat.getuVDecayEnd() - splat.getuVDecayStart();
		int numLsImgs = lsFrSpan * splat.getLifespanRepeat();
		int numDcImgs = dcFrSpan * splat.getDecayRepeat();



		int minNumLsFrames = lsFrSpan * splat.getLifespanRepeat();
		int minNumDcFrames = dcFrSpan * splat.getDecayRepeat();

		Vec4 colorDiff = new Vec4();
		colorDiff.set(splat.getMiddleRGBA()).sub(splat.getStartRGBA());
		int lsColorChange = (int)(Math.max(Math.max(Math.abs(colorDiff.x), Math.abs(colorDiff.y)), Math.max(Math.abs(colorDiff.z), Math.abs(colorDiff.w))) / 4);

		colorDiff.set(splat.getEndRGBA()).sub(splat.getMiddleRGBA());
		int dcColorChange = (int)(Math.max(Math.max(Math.abs(colorDiff.x), Math.abs(colorDiff.y)), Math.max(Math.abs(colorDiff.z), Math.abs(colorDiff.w))) / 4);


//		int numLsFrames = Math.max(minNumLsFrames, Math.min(64, (int)(splat.getLifespan()/timePerFrame)));
//		int numDcFrames = Math.max(minNumDcFrames, Math.min(64, (int)(splat.getDecay()/timePerFrame)));
		int numLsFrames = Math.max(minNumLsFrames, Math.min(lsColorChange, (int)(splat.getLifespan()/timePerFrame)));
		int numDcFrames = Math.max(minNumDcFrames, Math.min(dcColorChange, (int)(splat.getDecay()/timePerFrame)));


		float timePerLsFrame = splat.getLifespan()/numLsFrames;
		float timePerDcFrame = splat.getDecay()/numDcFrames;


		int numFrames = numLsFrames + numDcFrames;
		BufferedImage[] frames = new BufferedImage[numFrames];
		int[] delayTimes = new int[numFrames];



		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		Vec4 colorS = new Vec4(splat.getStartRGBA()).scale(1f/255f);
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f/255f);
		Vec4 colorE = new Vec4(splat.getEndRGBA()).scale(1f/255f);
		Vec4 tempCol = new Vec4();

		int cols = splat.getColumns();
		int width = image.getWidth() / cols;
		int rows = splat.getRows();
		int height = image.getHeight() / rows;

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);
//		System.out.println("numLsFrames:   " + numLsFrames + ",   numDcFrames:   " + numDcFrames);
//		System.out.println("lsColorChange: " + lsColorChange + ", dcColorChange: " + dcColorChange);

		for(int i = 0; i<numFrames; i++){
			int imgI;
			if(i<numLsFrames){
				float t = i*timePerLsFrame;
				delayTimes[i] = (int)(timePerLsFrame*100);
				float timeF = t / splat.getLifespan();
				imgI = ((int) (numLsImgs * timeF))%lsFrSpan + splat.getuVLifespanStart();
				tempCol.set(colorS).lerp(colorM, timeF);
			} else {
				float t = (i-numLsFrames)*timePerDcFrame;
				delayTimes[i] = (int)(timePerDcFrame*100);
				float timeF = t / splat.getDecay();
				imgI = ((int) (numDcImgs * timeF))%dcFrSpan + splat.getuVDecayStart();
				tempCol.set(colorM).lerp(colorE, timeF);
			}

			int xOff = (imgI % rows) * width;
			int yOff = (imgI / rows) * height;
			frames[i] = getTintedSubImage(image, tempCol, xOff, yOff, width, height);
		}

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);

//		System.out.println("generated gifIcon, w: " + width + ", h: " + height);
//		System.out.println("generated gifIcon: " + imageIcon);
		return getImageIcon(frames, delayTimes);
	}


	public static ImageIcon generateIcon2(UberSplatMappings.UberSplat splat){
		float timePerFrame = .1f;

		Vec4 colorDiff = new Vec4();
		colorDiff.set(splat.getMiddleRGBA()).sub(splat.getStartRGBA());
		int birthColorChange = (int)(Math.max(Math.max(Math.abs(colorDiff.x), Math.abs(colorDiff.y)), Math.max(Math.abs(colorDiff.z), Math.abs(colorDiff.w))) / 4);

		colorDiff.set(splat.getEndRGBA()).sub(splat.getMiddleRGBA());
		int decayColorChange = (int)(Math.max(Math.max(Math.abs(colorDiff.x), Math.abs(colorDiff.y)), Math.max(Math.abs(colorDiff.z), Math.abs(colorDiff.w))) / 4);


		int numBirthFrames = Math.max(1, Math.min(birthColorChange, (int)(splat.getBirthTime()/timePerFrame)));
//		int numPauseFrames = Math.max(1, Math.min(64, (int)(splat.getPauseTime()/timePerFrame)));
		int numPauseFrames = 1;
		int numDecayFrames = Math.max(1, Math.min(decayColorChange, (int)(splat.getDecay()/timePerFrame)));


		float timePerBirthFrame = splat.getBirthTime()/numBirthFrames;
		float timePerPauseFrame = splat.getPauseTime()/numPauseFrames;
		float timePerDecayFrame = splat.getDecay()/numDecayFrames;


		int numFrames = numBirthFrames + numPauseFrames + numDecayFrames;
		BufferedImage[] frames = new BufferedImage[numFrames];
		int[] delayTimes = new int[numFrames];



		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		BufferedImage image = BLPHandler.getImage(soundNameAndPaths[0][1] + ".blp");
		Vec4 colorS = new Vec4(splat.getStartRGBA()).scale(1f/255f);
		Vec4 colorM = new Vec4(splat.getMiddleRGBA()).scale(1f/255f);
		Vec4 colorE = new Vec4(splat.getEndRGBA()).scale(1f/255f);
		Vec4 tempCol = new Vec4();

		int cols = 1;
		int width = image.getWidth() / cols;
		int rows = 1;
		int height = image.getHeight() / rows;

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);

		for(int i = 0; i<numFrames; i++){
			if(i<numBirthFrames){
				float t = i*timePerBirthFrame;
				delayTimes[i] = (int)(timePerBirthFrame * 100);
				float timeF = t / splat.getBirthTime();
				tempCol.set(colorS).lerp(colorM, timeF);
			} else if (i == numBirthFrames) {
				delayTimes[i] = (int)(timePerPauseFrame*100);
				tempCol.set(colorM);
			} else {
				float t = (i - numBirthFrames - 1) * timePerDecayFrame;
				delayTimes[i] = (int)(timePerDecayFrame * 100);
				float timeF = t / splat.getDecay();
				tempCol.set(colorM).lerp(colorE, timeF);
			}

			frames[i] = getTintedSubImage(image, tempCol, 0, 0, width, height);
		}

//		System.out.println("totTime: " + totTime + ", timePerFrame: " + timePerFrame + ", numFrames: " + numFrames);
//		System.out.println("frames: " + frames.length + ", delays: " + delayTimes.length);

//		System.out.println("generated gifIcon, w: " + width + ", h: " + height);
//		System.out.println("generated gifIcon: " + imageIcon);
		return getImageIcon(frames, delayTimes);
	}

	public static ImageIcon getImageIcon(BufferedImage[] frames, int[] delayTimes){
		byte[] asBytes = TwiWriteGif.getAsBytes(frames, delayTimes);
		if(asBytes != null){
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
					sourceRaster.getPixel(w+xOff, h+yOff, srcPixelData);
					float alphaMul = srcPixelData[3]/255f * color.w;
					pixelData[0] = srcPixelData[0] * color.x * alphaMul + (1-alphaMul) * bgColor.x;
					pixelData[1] = srcPixelData[1] * color.y * alphaMul + (1-alphaMul) * bgColor.y;
					pixelData[2] = srcPixelData[2] * color.z * alphaMul + (1-alphaMul) * bgColor.z;
					pixelData[3] = 255f;
//					pixelData[3] = 255f * alphaMul + (1-alphaMul) * bgColor.w;
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
				int x2 = (xi + 1) * width-1;
				int y2 = (yi + 1) * height-1;
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
