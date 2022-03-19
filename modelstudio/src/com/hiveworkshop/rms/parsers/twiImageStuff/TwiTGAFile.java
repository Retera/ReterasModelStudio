package com.hiveworkshop.rms.parsers.twiImageStuff;

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
			imageId = ReaderUtils.getArray(stream, header.idFieldLength);
			colorMap = new ColorMap(header.colorMapFirstIndex, header.colorMapLength, header.colorMapPixelBits, ReaderUtils.getArray(stream, header.colorMapLength));
			numPixels = header.width * header.heigth;
			imageDataSize = numPixels * header.pixelDepth;
			System.out.println("imageId: " + Arrays.toString(imageId) + " (" + header.idFieldLength + " bytes)");
			imageData = new ImageData(header.width, header.heigth, header.pixelDepth, header.attributeBits, ReaderUtils.getArray(stream, imageDataSize), header.type);
		}
	}

	public BufferedImage getAsBufferedImage(){
		if(header != null){
			short width = header.width;
			short heigth = header.heigth;
			BufferedImage dst = new BufferedImage(width, heigth, header.hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();

			for (int h = 0; h < heigth; h++) {
				int hDest = header.isTopToBottom ? h : (heigth - 1 - h);
				for (int w = 0; w < width; w++) {
					int pixelInd = (h * width) + w;
					int wDest = header.isLeftToRight ? w : width - 1 - w;
					int pixelDest = (hDest * width) + wDest;
					pixels[pixelDest] = imageData.getPixelColor(pixelInd, header.type, colorMap);
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
				pixels[pixelDest] = imageData.getPixelColor(pixelInd, header.type, colorMap);
			}
		}
	}

	public static class Header {
		// header[0] - Length of the image ID field (0-255)
		// header[1] - Whether a color map is included (0/1)
		// header[2] - Compression and color types
		// header[3-7] - Color map specification

		// header[8-17] - Image specification
		// header[8-9] - X-origin
		// header[10-11] - Y-origin
		// header[12-13] - Image width
		// header[14-15] - Image height
		// header[16] - Pixel depth
		// header[17] - Image descriptor (1 byte): bits 3-0 give the alpha channel depth, bits 5-4 give direction (4: left-to-right ordering, 5: top-to-bottom ordering)
		byte idFieldLength;
		boolean hasColorMap;
		byte colorMapType;
		boolean hasAlpha;
		Type type;
		short colorMapFirstIndex;
		short colorMapLength;
		byte colorMapPixelBits;
		short xOrigin;
		short yOrigin;
		short width;
		short heigth;
		int pixelDepth;
		byte imageDescriptor;
		byte attributeBits;
		byte imageDirBits;
		boolean isLeftToRight;
		boolean isTopToBottom;
		//

		public Header(byte[] header){
			idFieldLength = header[0];
			hasColorMap = header[1] == 1;
			colorMapType = header[1];
			type = Type.getType(header[2]);
			colorMapFirstIndex = ReaderUtils.fromBytes(header[3], header[4]);
			colorMapLength = ReaderUtils.fromBytes(header[5], header[6]);
			colorMapPixelBits = header[7];
			xOrigin = ReaderUtils.fromBytes(header[8], header[9]);
			yOrigin = ReaderUtils.fromBytes(header[10], header[11]);
			width = ReaderUtils.fromBytes(header[12], header[13]);
			heigth = ReaderUtils.fromBytes(header[14], header[15]);
			pixelDepth = ReaderUtils.valueOfByte(header[16]);
			imageDescriptor = header[17];
			attributeBits = ReaderUtils.supByte(header[17], 3, 0);
			imageDirBits = ReaderUtils.supByte(header[17], 5, 4);
			isLeftToRight = ReaderUtils.supByte(header[17], 4, 4) == 1;
			isTopToBottom = ReaderUtils.supByte(header[17], 5, 5) == 1;

			hasAlpha = colorMapPixelBits==0 && attributeBits!=0 || colorMapPixelBits == 16 || colorMapPixelBits == 32;

			System.out.println("image type: " + type);
			System.out.println("image dir: " + imageDirBits + ", LeftToRight: " + isLeftToRight + ", TopToBottom: " + isTopToBottom);
		}
	}

	public static class ColorMap {
		short[][] colors;
		int[] intColors;
		public ColorMap(short colorMapFirstIndex, short colorMapLength, byte colorMapPixelBits, byte[] dataBytes){
			System.out.println("colorMapLength: " + colorMapLength);
			int bitsPerColor = Math.min(colorMapPixelBits/3, 8);
			int bytesPerEntry = ((colorMapPixelBits + 1)/8);
			int alphaSize = colorMapPixelBits - bitsPerColor*3;
			System.out.println("alphaSize: " + alphaSize);
			// Alpha should maybe be converted to 0/255 if colorMapPixelBits==16

			if(bitsPerColor != 0){
				int numColors = colorMapPixelBits/bitsPerColor + Math.min(1, alphaSize);
				colors = new short[colorMapLength][];
				intColors = new int[colorMapLength];

				for (int i = 0; i < colorMapLength; i++){
//				int temp = ReaderUtils.intFromBytes()
					byte[] tempBytes = new byte[bytesPerEntry];
					if (bytesPerEntry >= 0)
						System.arraycopy(dataBytes, i * bytesPerEntry, tempBytes, 0, bytesPerEntry);
					int col = ReaderUtils.intFromBytes(tempBytes);
					colors[i] = ReaderUtils.shortArrayOf(col, bitsPerColor, numColors);
					intColors[i] = 0;
					for (int j = 0; j< colors[i].length; j++){
						intColors[i] |= ((colors[i][j] & 0xFF) << (8*j));
					}
				}
			}
		}

		public int getColor(int i){
			return intColors[i];
		}
	}

	public static class ImageData {
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

	public enum Type {
		// header[2] - Compression and color types:
		//  0 - No Image Data Included.
		//  1 - Uncompressed, Color mapped image,
		//  2 - Uncompressed, True Color Image,
		//  3 - Uncompressed, Black and white image,
		//  9 - Run-length encoded, Color mapped image,
		// 11 - Run-Length encoded, Black and white image
		NO_IMAGE        ((byte)  0),
		UNCOMP_COL_MAP  ((byte)  1),
		UNCOMP_TRUE_COL ((byte)  2),
		UNCOMP_GREY     ((byte)  3),
		RUNL_COL_MAP    ((byte)  9),
		RUNL_TRUE_COL   ((byte) 10),
		RUNL_GREY       ((byte) 11),
		UNKNOWN       ((byte) -1),
		;
		byte b;
		Type(byte b){
			this.b = b;
		}

		public boolean isRunL(){
			return (b & (0x08)) != 0;
		}
		public byte getByte() {
			return b;
		}

		public static Type getType(byte b){
			return switch (b) {
				case  0 -> NO_IMAGE;
				case  1 -> UNCOMP_COL_MAP;
				case  2 -> UNCOMP_TRUE_COL;
				case  3 -> UNCOMP_GREY;
				case  9 -> RUNL_COL_MAP;
				case 10 -> RUNL_TRUE_COL;
				case 11 -> RUNL_GREY;
				default -> UNKNOWN;
			};
		}
	}
}
