package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import java.nio.ByteBuffer;

public class Compressor {

	public ByteBuffer CompressImageDXT5(int[] inBuf, int width, int height) {
		byte[] minColor = new byte[4];
		byte[] maxColor = new byte[4];
		int totBytes = (width/4 * height/4)*16;
		ByteBuffer globalOutData = ByteBuffer.allocateDirect(totBytes);

		long currInd;

		for ( int j = 0; j < height; j += 4) {
			for ( int i = 0; i < width; i += 4 ) {
				byte[][] block = getColorBlock(inBuf, j, i, width);
				GetMinMaxColorsBound2( block, minColor, maxColor );
				globalOutData.put(maxColor[3]);
				globalOutData.put(minColor[3]);

				byte[] alphaInd = getAlphaIndices(block, minColor, maxColor);
				for (int aI = 0; aI<alphaInd.length; aI++) {
					globalOutData.put(alphaInd[aI]);
				}

				int maxC = ColorTo565(maxColor);
				globalOutData.put((byte) (( maxC >>  0 ) & 255));
				globalOutData.put((byte) (( maxC >>  8 ) & 255));

				int minC = ColorTo565(minColor);
				globalOutData.put((byte) (( minC >>  0 ) & 255));
				globalOutData.put((byte) (( minC >>  8 ) & 255));

				long colorInd = getColorIndices(block, minColor, maxColor);
				globalOutData.put((byte) (( colorInd >>  0 ) & 255));
				globalOutData.put((byte) (( colorInd >>  8 ) & 255));
				globalOutData.put((byte) (( colorInd >> 16 ) & 255));
				globalOutData.put((byte) (( colorInd >> 24 ) & 255));

			}
		}
		return globalOutData;
	}


	int ColorTo565(byte[] color ) {
		return ((color[0] >> 3) << 11) | ((color[1] >> 2) << 5) | (color[2] >> 3);
	}

	private byte[][] getColorBlock(int[] inBuf, int img_row, int img_col, int width){
		byte[][] block = new byte[16][4];
		for ( int blockRow = 0; blockRow < 4; blockRow++ ) {
			for ( int blockCol = 0; blockCol < 4; blockCol++ ) {
				block[blockRow + blockCol] = getColorComps(inBuf[(img_row + blockRow)*width + (img_col + blockCol)]);
			}
		}
		return block;
	}

	private byte[] getColorComps(int color){
		byte[] data = new byte[4];
		data[0] = (byte) ((color >>  0) & 0xFF);
		data[1] = (byte) ((color >>  8) & 0xFF);
		data[2] = (byte) ((color >> 16) & 0xFF);
		data[3] = (byte) ((color >> 24) & 0xFF);
		return data;
	}

	void GetMinMaxColorsBound2(byte[][] colorBlock, byte[] minColor, byte[] maxColor ) {
		int INSET_SHIFT = 4;
		int i;
		byte[] inset = new byte[4];
		minColor[0] = minColor[1] = minColor[2] = (byte) 255;
		maxColor[0] = maxColor[1] = maxColor[2] = 0;
		for ( i = 0; i < 16; i++ ) {
			minColor[0] = DDSUtils.min(minColor[0], colorBlock[i][0]);
			minColor[1] = DDSUtils.min(minColor[1], colorBlock[i][1]);
			minColor[2] = DDSUtils.min(minColor[2], colorBlock[i][2]);
			minColor[3] = DDSUtils.min(minColor[3], colorBlock[i][3]);
			maxColor[0] = DDSUtils.max(maxColor[0], colorBlock[i][0]);
			maxColor[1] = DDSUtils.max(maxColor[1], colorBlock[i][1]);
			maxColor[2] = DDSUtils.max(maxColor[2], colorBlock[i][2]);
			maxColor[3] = DDSUtils.max(maxColor[3], colorBlock[i][3]);
//			minColor[0] = DDSUtils.min(minColor[0], colorBlock[i*4][0]);
//			minColor[1] = DDSUtils.min(minColor[1], colorBlock[i*4][1]);
//			minColor[2] = DDSUtils.min(minColor[2], colorBlock[i*4][2]);
//			minColor[3] = DDSUtils.min(minColor[3], colorBlock[i*4][3]);
//			maxColor[0] = DDSUtils.max(maxColor[0], colorBlock[i*4][0]);
//			maxColor[1] = DDSUtils.max(maxColor[1], colorBlock[i*4][1]);
//			maxColor[2] = DDSUtils.max(maxColor[2], colorBlock[i*4][2]);
//			maxColor[3] = DDSUtils.max(maxColor[3], colorBlock[i*4][3]);
		}

		inset[0] = (byte)(( maxColor[0] - minColor[0] ) >> INSET_SHIFT);
		inset[1] = (byte)(( maxColor[1] - minColor[1] ) >> INSET_SHIFT);
		inset[2] = (byte)(( maxColor[2] - minColor[2] ) >> INSET_SHIFT);
		inset[3] = (byte)(( maxColor[3] - minColor[3] ) >> INSET_SHIFT);

		minColor[0] = (byte)(Math.min(minColor[0] + inset[0], 255));
		minColor[1] = (byte)(Math.min(minColor[1] + inset[1], 255));
		minColor[2] = (byte)(Math.min(minColor[2] + inset[2], 255));
		minColor[3] = (byte)(Math.min(minColor[3] + inset[3], 255));

		maxColor[0] = (byte)(Math.max(0, maxColor[0] - inset[0]));
		maxColor[1] = (byte)(Math.max(0, maxColor[1] - inset[1]));
		maxColor[2] = (byte)(Math.max(0, maxColor[2] - inset[2]));
		maxColor[3] = (byte)(Math.max(0, maxColor[3] - inset[3]));
	}


	byte[] getAlphaIndices(byte[][] colorBlock, byte[] minColor, byte[] maxColor) {
		byte[] indices = new byte[16];
//		byte[] alphas = new byte[8];
		byte maxAlpha = minColor[3];
		byte minAlpha = maxColor[3];
//		alphas[0] = maxAlpha;
//		alphas[1] = minAlpha;
		byte mid = (byte) (( maxAlpha - minAlpha ) / ( 2 * 7 ));
		byte ab1 = (byte) (minAlpha + mid);
		byte ab2 = (byte) (( 6 * maxAlpha + 1 * minAlpha ) / 7 + mid);
		byte ab3 = (byte) (( 5 * maxAlpha + 2 * minAlpha ) / 7 + mid);
		byte ab4 = (byte) (( 4 * maxAlpha + 3 * minAlpha ) / 7 + mid);
		byte ab5 = (byte) (( 3 * maxAlpha + 4 * minAlpha ) / 7 + mid);
		byte ab6 = (byte) (( 2 * maxAlpha + 5 * minAlpha ) / 7 + mid);
		byte ab7 = (byte) (( 1 * maxAlpha + 6 * minAlpha ) / 7 + mid);
//		colorBlock += 3;
		for ( int i = 0; i < 16; i++ ) {
//			byte a = colorBlock[i][4];
			byte a = colorBlock[i][3];
			int b1 = ( a <= ab1 ) ? 1 : 0;
			int b2 = ( a <= ab2 ) ? 1 : 0;
			int b3 = ( a <= ab3 ) ? 1 : 0;
			int b4 = ( a <= ab4 ) ? 1 : 0;
			int b5 = ( a <= ab5 ) ? 1 : 0;
			int b6 = ( a <= ab6 ) ? 1 : 0;
			int b7 = ( a <= ab7 ) ? 1 : 0;
			int index = ( b1 + b2 + b3 + b4 + b5 + b6 + b7 + 1 ) & 7;
			int ugg = 2 > index ? 1: 0;
			indices[i] = (byte) (index ^ (ugg));
		}

		byte[] bb = new byte[6];
		bb[0] = ( (byte)((indices[ 0] >> 0) | (indices[ 1] << 3) | (indices[ 2] << 6) ));
		bb[1] = ( (byte)((indices[ 2] >> 2) | (indices[ 3] << 1) | (indices[ 4] << 4) | (indices[ 5] << 7) ));
		bb[2] = ( (byte)((indices[ 5] >> 1) | (indices[ 6] << 2) | (indices[ 7] << 5) ));
		bb[3] = ( (byte)((indices[ 8] >> 0) | (indices[ 9] << 3) | (indices[10] << 6) ));
		bb[4] = ( (byte)((indices[10] >> 2) | (indices[11] << 1) | (indices[12] << 4) | (indices[13] << 7) ));
		bb[5] = ( (byte)((indices[13] >> 1) | (indices[14] << 2) | (indices[15] << 5) ));

		long res = 0;
		for(int i = 0; i<6; i++){
			res = (res<<8)|bb[i];
		}

		return bb;
	}

	int C565_5_MASK = 0xF8;    // 0xFF minus last three bits
	int C565_6_MASK = 0xFC;    // 0xFF minus last two bits
	long getColorIndices(byte[][] colorBlock, byte[] minColor, byte[] maxColor){
		byte[][] colors = new byte[4][4];
		int[] indices = new int[16];
		colors[0][0] = (byte)(( maxColor[0] & C565_5_MASK ) | ( maxColor[0] >> 5 ));
		colors[0][1] = (byte)(( maxColor[1] & C565_6_MASK ) | ( maxColor[1] >> 6 ));
		colors[0][2] = (byte)(( maxColor[2] & C565_5_MASK ) | ( maxColor[2] >> 5 ));
		colors[1][0] = (byte)(( minColor[0] & C565_5_MASK ) | ( minColor[0] >> 5 ));
		colors[1][1] = (byte)(( minColor[1] & C565_6_MASK ) | ( minColor[1] >> 6 ));
		colors[1][2] = (byte)(( minColor[2] & C565_5_MASK ) | ( minColor[2] >> 5 ));
		colors[2][0] = (byte)(( 2 * colors[0][0] + 1 * colors[1][0] ) / 3);
		colors[2][1] = (byte)(( 2 * colors[0][1] + 1 * colors[1][1] ) / 3);
		colors[2][2] = (byte)(( 2 * colors[0][2] + 1 * colors[1][2] ) / 3);
		colors[3][0] = (byte)(( 1 * colors[0][0] + 2 * colors[1][0] ) / 3);
		colors[3][1] = (byte)(( 1 * colors[0][1] + 2 * colors[1][1] ) / 3);
		colors[3][2] = (byte)(( 1 * colors[0][2] + 2 * colors[1][2] ) / 3);
		for ( int i = 0; i < 16; i++ ) {
			int minDistance = 255;
			for ( int j = 0; j < 4; j++ ) {
//				int dist = ColorDistance(colorBlock[i*4], colors[j]);
				int dist = ColorDistance(colorBlock[i], colors[j]);
				if ( dist < minDistance ) {
					minDistance = dist;
					indices[i] = j;
				}
			}
		}
		long result = 0;
		for ( int i = 0; i < 16; i++ ) {
			result |= ( ((long)indices[i]) << ( i << 1 ) );
		}

		for ( int i = 15; i >= 0; i-- ) {
			int c0 = colorBlock[i][0];
			int c1 = colorBlock[i][1];
			int c2 = colorBlock[i][2];
//			int c0 = colorBlock[i*4][0];
//			int c1 = colorBlock[i*4][1];
//			int c2 = colorBlock[i*4][2];
			int d0 = Math.abs( colors[0][0] - c0 ) + Math.abs( colors[0][1] - c1 ) + Math.abs( colors[0][2] - c2 );
			int d1 = Math.abs( colors[1][0] - c0 ) + Math.abs( colors[1][1] - c1 ) + Math.abs( colors[1][2] - c2 );
			int d2 = Math.abs( colors[2][0] - c0 ) + Math.abs( colors[2][1] - c1 ) + Math.abs( colors[2][2] - c2 );
			int d3 = Math.abs( colors[3][0] - c0 ) + Math.abs( colors[3][1] - c1 ) + Math.abs( colors[3][2] - c2 );
			int b0 = d0 > d3 ? 1 : 0;
			int b1 = d1 > d2 ? 1 : 0;
			int b2 = d0 > d2 ? 1 : 0;
			int b3 = d1 > d3 ? 1 : 0;
			int b4 = d2 > d3 ? 1 : 0;
			int x0 = b1 & b2;
			int x1 = b0 & b3;
			int x2 = b0 & b4;
			result |= ( x2 | ( ( x0 | x1 ) << 1 ) ) << ( i << 1 );
		}

		return result;
	}
	byte[] getColorIndices2(byte[][] colorBlock, byte[] minColor, byte[] maxColor){
		byte[][] colors = new byte[4][4];
		int[] indices = new int[16];
		colors[0][0] = (byte)(( maxColor[0] & C565_5_MASK ) | ( maxColor[0] >> 5 ));
		colors[0][1] = (byte)(( maxColor[1] & C565_6_MASK ) | ( maxColor[1] >> 6 ));
		colors[0][2] = (byte)(( maxColor[2] & C565_5_MASK ) | ( maxColor[2] >> 5 ));
		colors[1][0] = (byte)(( minColor[0] & C565_5_MASK ) | ( minColor[0] >> 5 ));
		colors[1][1] = (byte)(( minColor[1] & C565_6_MASK ) | ( minColor[1] >> 6 ));
		colors[1][2] = (byte)(( minColor[2] & C565_5_MASK ) | ( minColor[2] >> 5 ));
		colors[2][0] = (byte)(( 2 * colors[0][0] + 1 * colors[1][0] ) / 3);
		colors[2][1] = (byte)(( 2 * colors[0][1] + 1 * colors[1][1] ) / 3);
		colors[2][2] = (byte)(( 2 * colors[0][2] + 1 * colors[1][2] ) / 3);
		colors[3][0] = (byte)(( 1 * colors[0][0] + 2 * colors[1][0] ) / 3);
		colors[3][1] = (byte)(( 1 * colors[0][1] + 2 * colors[1][1] ) / 3);
		colors[3][2] = (byte)(( 1 * colors[0][2] + 2 * colors[1][2] ) / 3);
		for ( int i = 0; i < 16; i++ ) {
			int minDistance = 255;
			for ( int j = 0; j < 4; j++ ) {
				int dist = ColorDistance(colorBlock[i*4], colors[j]);
				if ( dist < minDistance ) {
					minDistance = dist;
					indices[i] = j;
				}
			}
		}
		byte[] colorInd = new byte[16];
		for ( int i = 0; i < 16; i++ ) {
			colorInd[i] |= ( ((long)indices[i]) );
		}

		for ( int i = 15; i >= 0; i-- ) {
			int c0 = colorBlock[i*4][0];
			int c1 = colorBlock[i*4][1];
			int c2 = colorBlock[i*4][2];
			int d0 = Math.abs( colors[0][0] - c0 ) + Math.abs( colors[0][1] - c1 ) + Math.abs( colors[0][2] - c2 );
			int d1 = Math.abs( colors[1][0] - c0 ) + Math.abs( colors[1][1] - c1 ) + Math.abs( colors[1][2] - c2 );
			int d2 = Math.abs( colors[2][0] - c0 ) + Math.abs( colors[2][1] - c1 ) + Math.abs( colors[2][2] - c2 );
			int d3 = Math.abs( colors[3][0] - c0 ) + Math.abs( colors[3][1] - c1 ) + Math.abs( colors[3][2] - c2 );
			int b0 = d0 > d3 ? 1 : 0;
			int b1 = d1 > d2 ? 1 : 0;
			int b2 = d0 > d2 ? 1 : 0;
			int b3 = d1 > d3 ? 1 : 0;
			int b4 = d2 > d3 ? 1 : 0;
			int x0 = b1 & b2;
			int x1 = b0 & b3;
			int x2 = b0 & b4;
			colorInd[i] |= ( x2 | ( ( x0 | x1 ) << 1 ) );
		}

		return colorInd;
	}
	int ColorDistance(byte[] c1, byte[] c2 ) {
		return    ( c1[0] - c2[0] ) * ( c1[0] - c2[0] )
				+ ( c1[1] - c2[1] ) * ( c1[1] - c2[1] )
				+ ( c1[2] - c2[2] ) * ( c1[2] - c2[2] );
	}
}
