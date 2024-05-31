package de.wc3data.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

	/**
	 * Takes an images as input and generates an array containing this image and
	 * all possible mipmaps
	 */
	public static BufferedImage[] generateMipMaps(final BufferedImage input) {
		int curWidth = input.getWidth();
		int curHeight = input.getHeight();
		int maxDim = Math.max(curWidth, curHeight);
		int numMipMaps = 0;
		while ((int) Math.pow(2.0D, numMipMaps) < maxDim){
			numMipMaps++;
		}

		final BufferedImage[] result = new BufferedImage[numMipMaps];
		result[0] = input;
		for (int i = 1; i < numMipMaps; i++) {
			curWidth = Math.max(curWidth/2, 1);
			curHeight = Math.max(curHeight/2, 1);
			result[i] = ImageUtils.getScaledInstance(result[(i - 1)], curWidth, curHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
		}
		return result;
	}

	/**
	 * Scales an Image
	 *
	 * @param hint Rendering Hint
	 */
	public static BufferedImage getScaledInstance(final BufferedImage img, final int targetWidth, final int targetHeight, final Object hint, final boolean higherQuality) {
		final int type = img.getTransparency() == 1 ? 1 : 2;
		BufferedImage currImg = img;
		int w = higherQuality ? img.getWidth() : targetWidth;
		int h = higherQuality ? img.getHeight() : targetHeight;

		do {
			w = higherQuality && (targetWidth < w) ? Math.max(w / 2, targetWidth) : w;
			h = higherQuality && (targetHeight < h) ? Math.max(h / 2, targetHeight) : h;

			if (!img.getColorModel().hasAlpha()) {
				currImg = getScaledImage(currImg, w, h, hint, type);
			} else {
				//Necessary because otherwise BiLinear resize would cause transparent pixel to change color
				currImg = resizeWorkAround(currImg, w, h, hint);
			}

		} while ((w != targetWidth) || (h != targetHeight));
		return currImg;
	}

	private static BufferedImage resizeWorkAround(final BufferedImage ret, final int w, final int h, final Object hint) {

		final BufferedImage noAlpha = new BufferedImage(ret.getWidth(), ret.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < ret.getWidth(); x++) {
			for (int y = 0; y < ret.getHeight(); y++) {
				int color = ret.getRGB(x, y);
				color = color | 0xff000000;
				noAlpha.setRGB(x, y, color);
			}
		}
		final BufferedImage noAlphaSmall = getScaledImage(noAlpha, w, h, hint, BufferedImage.TYPE_INT_ARGB);
		final BufferedImage tmp = getScaledImage(ret, w, h, hint, BufferedImage.TYPE_INT_ARGB);
		noAlphaSmall.getAlphaRaster().setRect(0, 0, tmp.getAlphaRaster());

		return noAlphaSmall;
	}

	private static BufferedImage getScaledImage(BufferedImage image, int w, int h, Object hint, int type) {
		final BufferedImage scaledImage = new BufferedImage(w, h, type);
		Graphics2D g2 = scaledImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		g2.drawImage(image, 0, 0, w, h, null);
		g2.dispose();
		return scaledImage;
	}

	/**
	 * An alternative way to convert an image to type_byte_indexed (paletted)
	 * that avoids dithering.
	 */
	public static BufferedImage antiDitherConvert(final BufferedImage src) {
		final BufferedImage convertedImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				convertedImage.setRGB(x, y, src.getRGB(x, y));
			}
		}
		return convertedImage;
	}


	public static BufferedImage convertStandardImageType(final BufferedImage src, final boolean useAlpha) {
		int targetType = useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		if (src.getType() != targetType){
			return ImageUtils.changeImageType(src, targetType);
		} else {
			return src;
		}
	}
	public static BufferedImage changeImageType(final BufferedImage src, final int type) {
		final BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), type);
		final Graphics2D g = (Graphics2D) img.getGraphics();

		if (img.getColorModel().hasAlpha()) {
			final Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC);
			g.setComposite(comp);
		}

		g.drawImage(src, 0, 0, null);
		g.dispose();

		return img;
	}

	public static BufferedImage createBlank(final Color color, final int width, final int height) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics graphics = image.getGraphics();
		graphics.setColor(color);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();
		return image;
	}
}