package com.hiveworkshop.rms.parsers.twiImageStuff.TGA;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;

public class ImageData {
	// This field contains (Width)x(Height) pixels.
	// Each pixel specifies image data in one of the following formats:
	// a single color-map index for Pseudo-Color;
	// Attribute, Red, Green and Blue ordered data for TrueColor;
	// and independent color-map indices for Direct-Color.
	// The values for Width and Height are specified in header[12-15] respectively.
	// The number of attribute and color-definition bits for each pixel are defined in Fields 5.6 and 5.5,
	// respectively. Each pixel is stored as an integral number of bytes.
	byte[][] attribBytes;
	byte[][] pixelBytes;
	public ImageData(int w, int h, int pixelDepth, int attributeDepth, byte[] dataBytes, Type type) {
		int numPixels = w * h;
		int bitesPerPixel = pixelDepth;
		int bytesPerPixel = pixelDepth/8;

		attribBytes = new byte[numPixels][];
		pixelBytes = new byte[numPixels][];

		System.out.println("pixelDepth: " + pixelDepth);
		System.out.println("bytesPerPixel: " + bytesPerPixel);
		System.out.println("attributeDepth: " + attributeDepth);

		if(type.isRunL()){
			System.out.println("isRunL");
			int packages = dataBytes.length / (bytesPerPixel + 1);
			int pixInd = 0;
			int dataInd = 0;

			for(int i = 0; i<packages && pixInd<numPixels; i++){
				byte repsByte = dataBytes[dataInd];
				dataInd++;
				int dataStartInd = dataInd;
				int reps = (repsByte & 127) + 1;
				int pixAdj;
				if((repsByte & 128) != 0){
					dataInd += bytesPerPixel;
					pixAdj = 0;
				} else {
					dataInd += bytesPerPixel * reps;
					pixAdj = bytesPerPixel;
				}
				pixInd = readRunLData(attributeDepth, dataBytes, numPixels, bytesPerPixel, pixInd, dataStartInd, reps, pixAdj);

			}

		} else {
//			int bitesPerPixel = pixelDepth + attributeDepth;
			readPlainData(attributeDepth, dataBytes, numPixels, bytesPerPixel);
		}

	}

	private void getColorUgg(byte numPixelBits, byte[] dataBytes){
		int bytesPerEntry = ((numPixelBits + 1)/8);
		int bitsPerColor = Math.min(numPixelBits/3, 8);
		int alphaSize = numPixelBits - bitsPerColor*3;
		int numColors = numPixelBits/bitsPerColor + Math.min(1, alphaSize);

		short[][] colors = new short[dataBytes.length/bytesPerEntry][];

		for (int i = 0; i < dataBytes.length/bytesPerEntry; i++){
			byte[] tempBytes = new byte[bytesPerEntry];
			if (bytesPerEntry >= 0)
				System.arraycopy(dataBytes, i * bytesPerEntry, tempBytes, 0, bytesPerEntry);
			int col = ReaderUtils.intFromBytes(tempBytes);
			colors[i] = ReaderUtils.shortArrayOf(col, bitsPerColor, numColors);
		}

		int[] intColors = new int[colors.length];
		for (int i = 0; i < colors.length; i++){
			intColors[i] = 0;
			for (int j = 0; j< colors[i].length; j++){
				intColors[i] |= ((colors[i][j] & 0xFF) << (8*j));
			}
		}
	}

	public int readRunLData(int attributeDepth, byte[] dataBytes, int numPixels, int bytesPerPixel, int pixInd, int dataStartInd, int reps, int pixAdj) {
		for (int r = 0; r < reps && pixInd < numPixels; r++) {
			pixelBytes[pixInd] = new byte[bytesPerPixel];
			attribBytes[pixInd] = new byte[attributeDepth];

			for (int j = 0; j < bytesPerPixel; j++) {
				pixelBytes[pixInd][j] = dataBytes[dataStartInd + r * pixAdj + j];
			}
			for (int j = 0; j < attributeDepth; j++) {
				attribBytes[pixInd][j] = dataBytes[dataStartInd + r * pixAdj + j];
			}
			pixInd++;
		}
		return pixInd;
	}

	private void readPlainData(int attributeDepth, byte[] dataBytes, int numPixels, int bytesPerPixel) {
		for(int i = 0; i< numPixels; i++){
//					pixelBytes[i] = new byte[pixelDepth];
			pixelBytes[i] = new byte[bytesPerPixel];
			attribBytes[i] = new byte[attributeDepth];
			for (int j = 0; j< attributeDepth; j++){
				attribBytes[i][j] = dataBytes[i* bytesPerPixel +j];
			}
//				for (int j = attributeDepth; j<bytesPerPixel; j++){
//					pixelBytes[i][j] = dataBytes[i*bytesPerPixel + j];
//				}
			for (int j = 0; j< bytesPerPixel; j++){
				pixelBytes[i][j] = dataBytes[i* bytesPerPixel + j];
			}
//				for (int j = 0; j<pixelDepth; j++){
//					pixelBytes[i][j] = dataBytes[i*bytesPerPixel + j + attributeDepth];
//				}
		}
	}

	int getIntColor(int pixel){
		byte[] attribByte = attribBytes[pixel];
		byte[] pixelByte = pixelBytes[pixel];
		int attrLength = attribByte == null ? 0 : attribByte.length;
//			byte[] bytes = new byte[attrLength + pixelByte.length];
		byte[] bytes = new byte[pixelByte.length];

		for(int i = 0; i<pixelByte.length; i++){
			bytes[i] = pixelByte[i];
		}
//			for(int i = 0; i< attrLength; i++){
//				bytes[pixelByte.length + i] = attribByte[i];
//			}
//			for(int i = 0; i < attrLength; i++){
//				bytes[i] = attribByte[i];
//			}
//			for(int i = 0; i<pixelByte.length; i++){
//				bytes[attrLength + i] = pixelByte[i];
//			}
		return ReaderUtils.intFromBytes(bytes);
	}

	public int getPixelColor(int pixel, Type type, ColorMap colorMap){
		return switch (type){
			case NO_IMAGE -> 0;
			case UNCOMP_COL_MAP -> colorMap.getColor(attribBytes[pixel][0]);
			case UNCOMP_TRUE_COL -> getIntColor(pixel);
			case UNCOMP_GREY -> getIntColor(pixel);
			case RUNL_COL_MAP -> colorMap.getColor(attribBytes[pixel][0]);
			case RUNL_TRUE_COL -> getIntColor(pixel);
			case RUNL_GREY -> 0;
			case UNKNOWN -> 0;
		};
	}
}
