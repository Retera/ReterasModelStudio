package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ImageCreator {
	private Image image;

	public ImageCreator(int width, int height){
		image = new BufferedImage(width, height, ColorSpace.CS_sRGB);
	}

	public static BufferedImage getBufferedImage(Material material, final DataSource workingDirectory) {
//		System.out.println("getBufferedImage");
		BufferedImage theImage = null;
		if (Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString()) && (material.getLayers().size() > 0)) {
			final Layer firstLayer = material.getLayer(0);
			final Bitmap tex = firstLayer.firstTexture();
			final String path = getRenderableTexturePath(tex);
			BufferedImage newImage;
			try {
//				newImage = BLPHandler.get().getTexture(workingDirectory, path);
				newImage = BLPHandler.getImage(tex, workingDirectory);
			} catch (final Exception exc) {
				// newImage = null;
				newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
			}
			return newImage;
		} else {
            for (final Layer lay : material.getLayers()) {
                final Bitmap tex = lay.firstTexture();
                final String path = getRenderableTexturePath(tex);
                BufferedImage newImage;
                try {
//                    newImage = BLPHandler.get().getTexture(workingDirectory, path);
                    newImage = BLPHandler.getImage(tex, workingDirectory);
                } catch (final Exception exc) {
                    // newImage = null;
                    newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                }
                if (theImage == null) {
                    theImage = newImage;
                } else {
                    if (newImage != null) {
                    	if (tex != null) System.out.println(tex.getName());
                    	if(lay.getFilterMode() == MdlxLayer.FilterMode.MODULATE){
		                    System.out.println("modulate!");
                    		theImage = modulate(theImage, newImage);
	                    } else if(lay.getFilterMode() == MdlxLayer.FilterMode.MODULATE2X) {
		                    theImage = modulateX2(theImage, newImage);
	                    } else if(lay.getFilterMode() == MdlxLayer.FilterMode.ADDITIVE) {
		                    theImage = additative(theImage, newImage);
	                    } else {
		                    theImage = mergeImage(theImage, newImage);
	                    }
                    }
                }
            }
		}

		return theImage;
	}

	public static BufferedImage mergeImage(final BufferedImage source, final BufferedImage overlay) {
		final int w = Math.max(source.getWidth(), overlay.getWidth());
		final int h = Math.max(source.getHeight(), overlay.getHeight());
		final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, w, h, null);
		g.drawImage(overlay, 0, 0, w, h, null);

		return combined;
	}

	public static BufferedImage mergeImageScaled(final Image source, final Image overlay, final int w1, final int h1,
	                                             final int w2, final int h2) {
		final int w = Math.max(w1, w2);
		final int h = Math.max(h1, h2);
		final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, w1, h1, null);
		g.drawImage(overlay, (w1 - w2) / 2, (h1 - h2) / 2, w2, h2, null);

		return combined;
	}

	private void ugg(BufferedImage in, BufferedImage in2){
		Graphics graphics = image.getGraphics();
		ImageFilter imageFilter = new ImageFilter();
		ImageProducer imageProducer = new ImageProducer() {
			@Override
			public void addConsumer(ImageConsumer ic) {

			}

			@Override
			public boolean isConsumer(ImageConsumer ic) {
				return false;
			}

			@Override
			public void removeConsumer(ImageConsumer ic) {

			}

			@Override
			public void startProduction(ImageConsumer ic) {

			}

			@Override
			public void requestTopDownLeftRightResend(ImageConsumer ic) {

			}
		};
	}

	public BufferedImage getBufferedImage(final DataSource workingDirectory) {
//		System.out.println("getBufferedImage");
		BufferedImage theImage = null;
		if (true)
//		(SHADER_HD_DEFAULT_UNIT.equals(shaderString) && (layers.size() > 0)) {
//			Layer firstLayer = layers.get(0);
//			Bitmap tex = firstLayer.firstTexture();
//			String path = getRenderableTexturePath(tex);
//
//			BufferedImage newImage;
//			try {
//				newImage = BLPHandler.get().getTexture(workingDirectory, path);
//			} catch (final Exception exc) {
//				// newImage = null;
//				newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
//			}
//			return newImage;
//		} else
			{
			ArrayList<Bitmap> bitmaps = new ArrayList<>();
			for (Bitmap tex : bitmaps) {
				String path = getRenderableTexturePath(tex);
				BufferedImage newImage;
				try {
//					newImage = BLPHandler.get().getTexture(workingDirectory, path);
					newImage = BLPHandler.getImage(tex,workingDirectory);
				} catch (final Exception exc) {
					// newImage = null;
					newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
				}
				if (theImage == null) {
					theImage = newImage;
				} else {
					if (newImage != null) {
						theImage = mergeImage(theImage, newImage);
					}
				}
			}
		}

		return theImage;
	}


	public static BufferedImage ugg(WritableRaster raster1, WritableRaster raster2){
		if(raster1.getHeight() == raster2.getHeight() && raster1.getWidth() == raster2.getWidth()){
			BufferedImage combined = new BufferedImage(raster1.getWidth(), raster1.getHeight(), BufferedImage.TYPE_INT_ARGB);
			WritableRaster combinedRaster = combined.getRaster();

			for(int h = 0; h<raster1.getHeight(); h++){
				for(int w = 0; w<raster1.getWidth(); w++){
					float[] pixelData1 = new float[raster1.getNumBands()];
					float[] pixelData2 = new float[raster2.getNumBands()];
					raster1.getPixel(w, h, pixelData1);
					raster2.getPixel(w, h, pixelData2);

					int min = Math.min(pixelData1.length, pixelData2.length);
					float[] pixelData = new float[min];
					for(int pd = 0; pd< min; pd++){
						pixelData[pd] = pixelData1[pd]*pixelData2[pd];
					}
					combinedRaster.setPixel(w, h, pixelData);
				}
			}
			combined.setData(combinedRaster);
			return combined;
		}
		return null;
	}

	public static BufferedImage getScaledImage(BufferedImage source, int w, int h) {
		if(source.getWidth() != w || source.getHeight() != h){
			System.out.println("scaling Image");
			BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Graphics g = scaled.getGraphics();
			g.drawImage(source, 0, 0, w, h, null);
			g.dispose();
			return scaled;
		}
		return source;
	}


	public static BufferedImage modulate(BufferedImage image1, BufferedImage image2){
		int height = Math.max(image1.getHeight(), image2.getHeight());
		int width = Math.max(image1.getWidth(), image2.getWidth());
		Raster raster1 = getScaledImage(image1, width, height).getData();
		Raster raster2 = getScaledImage(image2, width, height).getData();

		BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster combinedRaster = combined.getRaster();

		for(int h = 0; h< height; h++){
			for(int w = 0; w< width; w++){
				float[] pixelData1 = new float[raster1.getNumBands()];
				float[] pixelData2 = new float[raster2.getNumBands()];
				raster1.getPixel(w, h, pixelData1);
				raster2.getPixel(w, h, pixelData2);

				int min = Math.min(pixelData1.length, pixelData2.length);
				float[] pixelData = new float[min];
				float addAlpha = 1;
				for(int pd = 0; pd< min; pd++){
					pixelData[pd] = Math.min(1f, (pixelData1[pd]/255f)*(pixelData2[pd]/255f))*255f;
					addAlpha *= (pixelData2[pd]/255f);
				}
//				pixelData[3] = Math.min(255, pixelData1[3] - addAlpha);
//				pixelData[3] = Math.min(255, pixelData1[3] * (addAlpha));
//				System.out.println("pixelData1: " + Arrays.toString(pixelData1) + ", addAlpha: " + addAlpha);
				pixelData[3] = 255f - Math.min(1f, ((255-pixelData1[3])/255f) * ((addAlpha)))*255f;
//				System.out.println("pixD: " + Arrays.toString(pixelData));
				combinedRaster.setPixel(w, h, pixelData);
			}
		}
		combined.setData(combinedRaster);
		return combined;
	}
	public static BufferedImage modulateX2(BufferedImage image1, BufferedImage image2){
		int height = Math.max(image1.getHeight(), image2.getHeight());
		int width = Math.max(image1.getWidth(), image2.getWidth());
		Raster raster1 = getScaledImage(image1, width, height).getData();
		Raster raster2 = getScaledImage(image2, width, height).getData();

		BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster combinedRaster = combined.getRaster();

		for(int h = 0; h< height; h++){
			for(int w = 0; w< width; w++){
				float[] pixelData1 = new float[raster1.getNumBands()];
				float[] pixelData2 = new float[raster2.getNumBands()];
				raster1.getPixel(w, h, pixelData1);
				raster2.getPixel(w, h, pixelData2);

				int min = Math.min(pixelData1.length, pixelData2.length);
				float[] pixelData = new float[min];
				float addAlpha = 1;
				for(int pd = 0; pd< min; pd++){
					pixelData[pd] = Math.min(1f, (pixelData1[pd]/255f)*(2*pixelData2[pd]/255f))*255f;
					addAlpha *= (2*pixelData2[pd]/255f);
				}
//				pixelData[3] = Math.min(255, pixelData1[3] - addAlpha);
//				pixelData[3] = Math.min(255, pixelData1[3] * (addAlpha));
				pixelData[3] = 255f - Math.min(1f, ((255-pixelData1[3])/255f) * ((addAlpha)))*255f;
				combinedRaster.setPixel(w, h, pixelData);
			}
		}
		combined.setData(combinedRaster);
		return combined;
	}
	public static BufferedImage additative(BufferedImage image1, BufferedImage image2){
		int height = Math.max(image1.getHeight(), image2.getHeight());
		int width = Math.max(image1.getWidth(), image2.getWidth());
		Raster raster1 = getScaledImage(image1, width, height).getData();
		Raster raster2 = getScaledImage(image2, width, height).getData();

		BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster combinedRaster = combined.getRaster();

		for(int h = 0; h< height; h++){
			for(int w = 0; w< width; w++){
				float[] pixelData1 = new float[raster1.getNumBands()];
				float[] pixelData2 = new float[raster2.getNumBands()];
				raster1.getPixel(w, h, pixelData1);
				raster2.getPixel(w, h, pixelData2);

				int min = Math.min(pixelData1.length, pixelData2.length);
				float[] pixelData = new float[min];
				float addAlpha = 0;
				for(int pd = 0; pd< min-1; pd++){
					pixelData[pd] = Math.min(255, pixelData1[pd] + pixelData2[pd]);
					addAlpha += pixelData2[pd];
				}
				pixelData[3] = Math.min(255, pixelData1[3] + addAlpha);
				combinedRaster.setPixel(w, h, pixelData);
			}
		}
		combined.setData(combinedRaster);
		return combined;
	}


	/**
	 * Intended to handle resolving ReplaceableIds into paths
	 */
	private static String getRenderableTexturePath(final Bitmap tex) {
		if (tex == null) {
			return "Textures\\white.blp";
		}
		if (tex.getPath().length() == 0) {
			return getReplaceableTexturePath(tex);
		}
		return tex.getPath();
	}
	private static String getRenderableTexturePath1(final Bitmap tex) {
		if (tex == null) {
			return "Textures\\white.blp";
		}
		if (tex.getPath().length() == 0) {
			String tcString = ("" + (100 + Material.teamColor)).substring(1);
			return switch (tex.getReplaceableId()){
				case 0 -> "";
				case 1 -> "ReplaceableTextures\\TeamColor\\TeamColor" + tcString + ".blp";
				case 2 -> "ReplaceableTextures\\TeamGlow\\TeamGlow" + tcString + ".blp";
				case 11 -> "ReplaceableTextures\\Cliff\\Cliff0" + ".blp";
				default -> "replaceabletextures\\lordaerontree\\lordaeronsummertree" + ".blp";
			};
		}
		return tex.getPath();
	}



	private static String getReplaceableTexturePath(Bitmap bitmap) {
		String tcString = ("" + (100 + Material.teamColor)).substring(1);
		return switch (bitmap.getReplaceableId()){
			case 0 -> "";
			case 1 -> "ReplaceableTextures\\TeamColor\\TeamColor" + tcString + ".blp";
			case 2 -> "ReplaceableTextures\\TeamGlow\\TeamGlow" + tcString + ".blp";
			case 11 -> "ReplaceableTextures\\Cliff\\Cliff0" + ".blp";
			default -> "replaceabletextures\\lordaerontree\\lordaeronsummertree" + ".blp";
		};
	}


	public BufferedImage getBufferedImage(int width, int height, BufferedImage image1, BufferedImage image2 ) {
		try {
			final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			// paintComponent(image.getGraphics(),5);
			final Pbuffer buffer = new Pbuffer(width, height, new PixelFormat(), null, null);
			buffer.makeCurrent();
			final ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);


//			GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL_UNSIGNED_BYTE, pixels);

			final int[] array = new int[pixels.capacity() / 4];
			final int[] flippedArray = new int[pixels.capacity() / 4];

			pixels.asIntBuffer().get(array);
			for (int i = 0; i < array.length; i++) {
				final int rgba = array[i];
				final int a = rgba & 0xFF;
				array[i] = (rgba >>> 8) | (a << 24);
			}

			for (int i = 0; i < height; i++) {
				System.arraycopy(array, i * width, flippedArray, (height - 1 - i) * width, width);
			}
			image.getRaster().setDataElements(0, 0, width, height, flippedArray);

			buffer.releaseContext();
			return image;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage forceBufferedImagesRGB(final BufferedImage in) {
		// Resolve input ColorSpace.
		ColorSpace inCS = in.getColorModel().getColorSpace();
		ColorSpace sRGBCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		if (inCS == sRGBCS) {
			// Already is sRGB.
			return in;
		}
		if (inCS.getNumComponents() != sRGBCS.getNumComponents()) {
			throw new IllegalArgumentException("Input color space has different number of components from sRGB.");
		}

		// Draw input.
		ColorModel lRGBModel = new ComponentColorModel(inCS, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		ColorModel sRGBModel = new ComponentColorModel(sRGBCS, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		BufferedImage lRGB = new BufferedImage(lRGBModel, lRGBModel.createCompatibleWritableRaster(in.getWidth(), in.getHeight()), false, null);

		for (int i = 0; i < in.getWidth(); i++) {
			for (int j = 0; j < in.getHeight(); j++) {
				lRGB.setRGB(i, j, in.getRGB(i, j));
			}
		}

		// Convert to sRGB.

		return new BufferedImage(sRGBModel, lRGB.getRaster(), false, null);
	}
}
