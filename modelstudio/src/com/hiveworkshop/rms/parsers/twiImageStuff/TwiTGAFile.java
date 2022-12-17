package com.hiveworkshop.rms.parsers.twiImageStuff;

import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.ColorMap;
import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.Header;
import com.hiveworkshop.rms.parsers.twiImageStuff.TGA.ImageData;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.util.Arrays;

public class TwiTGAFile {
	Header header;
	byte[] imageId;
	ColorMap colorMap;
	int numPixels;
	int imageDataSize;
	ImageData imageData;

	public TwiTGAFile(InputStream stream){
		if(stream != null){
			header = new Header(ReaderUtils.getArray(stream, 18));
			imageId = ReaderUtils.getArray(stream, header.getIdFieldLength());
			colorMap = new ColorMap(header.getColorMapFirstIndex(), header.getColorMapLength(), header.getColorMapPixelBits(), ReaderUtils.getArray(stream, header.getColorMapLength()));
			numPixels = header.getWidth() * header.getHeigth();
			imageDataSize = numPixels * header.getPixelDepth();
			System.out.println("imageId: " + Arrays.toString(imageId) + " (" + header.getIdFieldLength() + " bytes)");
			imageData = new ImageData(header.getWidth(), header.getHeigth(), header.getPixelDepth(), header.getAttributeBits(), ReaderUtils.getArray(stream, imageDataSize), header.getType());
		}
	}

	public TwiTGAFile(Header header, byte[] imageId, ColorMap colorMap, ImageData imageData){
		if(header != null){
			this.header = header;
			this.imageId = imageId;
			this.colorMap = colorMap;
			this.numPixels = header.getWidth() * header.getHeigth();
			this.imageDataSize = numPixels * header.getPixelDepth();
			this.imageData = imageData;
		}
	}

	public BufferedImage getAsBufferedImage(){
		if(header != null){
			short width = header.getWidth();
			short heigth = header.getHeigth();
			BufferedImage dst = new BufferedImage(width, heigth, header.isHasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();

			for (int h = 0; h < heigth; h++) {
				int hDest = header.isTopToBottom() ? h : (heigth - 1 - h);
				for (int w = 0; w < width; w++) {
					int pixelInd = (h * width) + w;
					int wDest = header.isLeftToRight() ? w : width - 1 - w;
					int pixelDest = (hDest * width) + wDest;
					pixels[pixelDest] = imageData.getPixelColor(pixelInd, header.getType(), colorMap);
				}
			}

			return dst;
		}

//		BufferedImage dst = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//		((DataBufferInt) dst.getRaster().getDataBuffer()).getData()[0] = 0;
//		return dst;
		return null;
	}

	public void setPixelData(short width, short heigth, int[] pixels, boolean leftToRight, boolean topToBottom) {
		for (int h = 0; h < heigth; h++) {
			int hDest = topToBottom ? h : (heigth - 1 - h);
			for (int w = 0; w < width; w++) {
				int pixelInd = (h * width) + w;
				int wDest = leftToRight ? w : width - 1 - w;
				int pixelDest = (hDest * width) + wDest;
				pixels[pixelDest] = imageData.getPixelColor(pixelInd, header.getType(), colorMap);
			}
		}
	}

}
