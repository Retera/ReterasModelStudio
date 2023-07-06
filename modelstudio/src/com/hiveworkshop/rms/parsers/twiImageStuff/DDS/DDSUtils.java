package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

public class DDSUtils {
	public static final int C565_5_MASK = 0xF8;    // 0xFF minus last three bits
	public static final int C565_6_MASK = 0xFC;    // 0xFF minus last two bits

	public static int ColorDistance(byte[] c1, byte[] c2) {
		return    ( c1[0] - c2[0] ) * ( c1[0] - c2[0] )
				+ ( c1[1] - c2[1] ) * ( c1[1] - c2[1] )
				+ ( c1[2] - c2[2] ) * ( c1[2] - c2[2] );
	}

	public static byte[] getColorComps(int color){
		byte[] data = new byte[4];
		data[0] = (byte) ((color >>  0) & 0xFF);
		data[1] = (byte) ((color >>  8) & 0xFF);
		data[2] = (byte) ((color >> 16) & 0xFF);
		data[3] = (byte) ((color >> 24) & 0xFF);
		return data;
	}


	public static int ColorTo565(byte[] color ) {
		int r = ((int)color[0]) >> 3;
		int g = ((int)color[1]) >> 2;
		int b = ((int)color[2]) >> 3;
		System.out.println("r: " + color[0] + ", g: " + color[1] + ", b: " + color[2]);
		System.out.println("r: " + r + ", g: " + g + ", b: " + b);
		return (r << 11) | (g << 5) | (b << 0);
	}

	public static int ColorTo565(int[] color ) {
		int r = (color[0]) >> 3;
		int g = (color[1]) >> 2;
		int b = (color[2]) >> 3;
//		System.out.println("r: " + color[0] + ", g: " + color[1] + ", b: " + color[2]);
		System.out.println("r: " + r + ", g: " + g + ", b: " + b + ", -> " + ((r << 11) | (g << 5) | (b << 0)));
		return (r << 11) | (g << 5) | (b << 0);
	}


	public static int ColorTo565_1(float[] color ) {
		int c0 = (int) (color[0]*255);
		int c1 = (int) (color[1]*255);
		int c2 = (int) (color[2]*255);
		return ((c0 >> 3) << 11) | ((c1 >> 2) << 5) | (c2 >> 3);
	}
	public static int ColorTo565_2(float[] color ) {
//		int c0 = (int) (color[0]*255);
//		int c1 = (int) (color[1]*255);
//		int c2 = (int) (color[2]*255);
		int c0 = (int) (color[0]);
		int c1 = (int) (color[1]);
		int c2 = (int) (color[2]);
		int r = ((c0*4)&255) >> 3;
		int g = ((c1*4)&255) >> 2;
		int b = ((c2*4)&255) >> 3;
//		System.out.println("r: " + color[0] + ", g: " + color[1] + ", b: " + color[2]);
		System.out.println("r: " + c0 + ", g: " + c1 + ", b: " + c2);
//		System.out.println("r: " + r + ", g: " + g + ", b: " + b + ", -> " + ((r << 11) | (g << 5) | (b << 0)));
		return (r << 11) | (g << 5) | (b << 0);
	}
	public static int ColorTo565(float[] color ) {
//		int c0 = (int) (color[0]*255);
//		int c1 = (int) (color[1]*255);
//		int c2 = (int) (color[2]*255);
		int c0 = (int) (color[0]);
		int c1 = (int) (color[1]);
		int c2 = (int) (color[2]);
		int r = (c0 & 255) >> 3;
		int g = (c1 & 255) >> 2;
		int b = (c2 & 255) >> 3;
//		System.out.println("r: " + color[0] + ", g: " + color[1] + ", b: " + color[2]);
//		System.out.println("r: " + c0 + ", g: " + c1 + ", b: " + c2 + ", -> " + ((r << 11) | (g << 5) | (b << 0)));
//		System.out.println("r: " + r + ", g: " + g + ", b: " + b + ", -> " + ((r << 11) | (g << 5) | (b << 0)));
		return (r << 11) | (g << 5) | (b << 0);
	}

	public static int[] getColorComps565(int color){
		int[] data = new int[4];
		data[0] = (((color >>  0) & C565_5_MASK) << 3);
		data[1] = (((color >>  5) & C565_6_MASK) << 2);
		data[2] = (((color >> 11) & C565_5_MASK) << 3);
		data[3] = ((color >> 24) & 0xFF);
		return data;
	}
	public static byte[] getColorComps565__(int color){
		byte[] data = new byte[4];
		data[0] = (byte) (((color >>  0) & 0xFF) >> 3);
		data[1] = (byte) (((color >>  8) & 0xFF) >> 2);
		data[2] = (byte) (((color >> 16) & 0xFF) >> 3);
		data[3] = (byte) ((color >> 24) & 0xFF);
		return data;
	}
	public static int[] getColorCompsFrom565(int color) {
		int[] data = new int[4];

//		return (r << 11) | (g << 5) | (b << 0);
		data[2] = (((color >>  0) & 63)*8);
		data[1] = (((color >>  5) & 127)*4);
		data[0] = (((color >> 11) & 63)*8);
//		data[2] = (((color >>  0) &  64)*8);
//		data[1] = (((color >>  5) & 128)*4);
//		data[0] = (((color >> 11) &  64)*8);
		return data;
	}

	public static int ColorFrom565(byte[] color) {
		return (color[0] << 11) | (color[1] << 5) | (color[2]);
	}

	public static int ColorLuminance(byte[] color) {
		return ( color[0] + color[1] * 2 + color[2] );
	}

	public static byte min(byte a, byte b){
		if (a < b) return a;
		return b;
	}

	public static byte max(byte a, byte b){
		if (a < b) return b;
		return a;
	}
}
