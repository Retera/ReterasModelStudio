package com.hiveworkshop.rms.parsers.twiImageStuff.TGA;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;

public class ColorMap {
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