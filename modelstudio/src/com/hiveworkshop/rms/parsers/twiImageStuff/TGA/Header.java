package com.hiveworkshop.rms.parsers.twiImageStuff.TGA;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;

public class Header {
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

	public byte getIdFieldLength() {
		return idFieldLength;
	}

	public boolean isHasColorMap() {
		return hasColorMap;
	}

	public byte getColorMapType() {
		return colorMapType;
	}

	public boolean isHasAlpha() {
		return hasAlpha;
	}

	public Type getType() {
		return type;
	}

	public short getColorMapFirstIndex() {
		return colorMapFirstIndex;
	}

	public short getColorMapLength() {
		return colorMapLength;
	}

	public byte getColorMapPixelBits() {
		return colorMapPixelBits;
	}

	public boolean isValidColorMapPixelBits() {
		return colorMapPixelBits == 0
				|| colorMapPixelBits == 15
				|| colorMapPixelBits == 16
				|| colorMapPixelBits == 24
				|| colorMapPixelBits == 32;
	}

	public short getxOrigin() {
		return xOrigin;
	}

	public short getyOrigin() {
		return yOrigin;
	}

	public short getWidth() {
		return width;
	}

	public short getHeigth() {
		return heigth;
	}

	public int getPixelDepth() {
		return pixelDepth;
	}

	public boolean isValidPixelDepth() {
		return pixelDepth == 8
				|| pixelDepth == 16
				|| pixelDepth == 24
				|| pixelDepth == 32;
	}

	public byte getImageDescriptor() {
		return imageDescriptor;
	}

	public byte getAttributeBits() {
		return attributeBits;
	}

	public byte getImageDirBits() {
		return imageDirBits;
	}

	public boolean isLeftToRight() {
		return isLeftToRight;
	}

	public boolean isTopToBottom() {
		return isTopToBottom;
	}
}
