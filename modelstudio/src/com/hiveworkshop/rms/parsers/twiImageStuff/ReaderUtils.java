package com.hiveworkshop.rms.parsers.twiImageStuff;

import java.io.*;

public class ReaderUtils {
	public static short fromBytes(byte b1, byte b2){
		short s = 0;
		s |= (b1 & 0xFF);
		s |= (b2 & 0xFF) << 8;
		return s;
	}
	public static short shortFromBytes(byte... bytes){
		short s = 0;
		for(int i = 0; i<2 && i<bytes.length; i++){
			s |= (bytes[i] & 0xFF) << (8*i);
		}
		return s;
	}
	public static int fromBytes(byte b1, byte b2, byte b3){
		int s = 0;
		s |= (b1 & 0xFF);
		s |= (b2 & 0xFF) << 8;
		s |= (b3 & 0xFF) << 16;
		return s;
	}
	public static int fromBytes(byte b1, byte b2, byte b3, byte b4){
		int s = 0;
		s |= (b1 & 0xFF);
		s |= (b2 & 0xFF) << 8;
		s |= (b3 & 0xFF) << 16;
		s |= (b4 & 0xFF) << 24;
		return s;
	}
	public static int intFromBytes(byte... bytes){
		int s = 0;
		for(int i = 0; i<4 && i<bytes.length; i++){
			s |= (bytes[i] & 0xFF) << (8*i);
		}
		return s;
	}

	public static byte supByte(byte b, int start, int end){
		byte nibble = 0;
		int step = (int) Math.copySign(1.2, end-start);
		for(int bi = start, ni = 0; ni <= Math.abs(end-start); bi+= step, ni++){
			int temp = ((b & (1<<bi)) >> (bi));
			nibble |= (temp << (ni));
		}
		return nibble;
	}

	public static byte supByte(int b, int start, int end){
		byte nibble = 0;
		int step = (int) Math.copySign(1.2, end-start);
		int length = Math.min(7, Math.abs(end - start));
		for(int bi = start, ni = 0; ni <= length; bi+= step, ni++){
			int temp = ((b & (1<<bi)) >> (bi));
			nibble |= (temp << (ni));
		}
		return nibble;
	}

	public static int supByteValue(byte b, int start, int end){
		byte nibble = 0;
		int step = (int) Math.copySign(1.2, end-start);
		for(int bi = start, ni = 0; ni <= Math.abs(end-start); bi+= step, ni++){
			int temp = ((b & (1<<bi)) >> (bi));
			nibble |= (temp << (ni));
		}
		return ((256+nibble)%256);
	}

	public static int supByteValue(int b, int start, int end){
		int nibble = 0;
		int step = (int) Math.copySign(1.2, end-start);
		for(int bi = start, ni = 0; ni <= Math.abs(end-start); bi+= step, ni++){
			int temp = ((b & (1<<bi)) >> (bi));
//			nibble |= (temp << (ni));
			nibble += (temp << (ni));
		}
//		return ((256+nibble)%256);
		return nibble;
	}

	public static byte[] byteArrayOf(int v, int bitsPerByte, int totBytes){
		byte[] a = new byte[totBytes];
		for(int i = 0; i<totBytes; i++){
			a[i] = supByte(v, i*bitsPerByte, (i+1)*bitsPerByte);
		}
		return a;
	}

	public static short[] shortArrayOf(int v, int bitsPerByte, int totBytes){
		short[] a = new short[totBytes];
		for(int i = 0; i<totBytes; i++){
			a[i] = (short) ((256 + supByte(v, i*bitsPerByte, (i+1)*bitsPerByte)) % 256);
		}
		return a;
	}

	public static int valueOfByte(byte b){
		return ((256+b)%256);
	}

	public static byte[] getArray(InputStream stream, int size){
		if(size == -1){
			try {
				return stream.readAllBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			byte[] array = new byte[size];

			try {
				int read = stream.read(array);
				for(int i = Math.max(0, read); i < size; i++){
					array[i] = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
				for(int i = 0; i < size; i++){
					array[i] = 0;
				}
			}
			return array;
		}
		return new byte[0];
	}

	public static int[] getIntArray(InputStream stream, int size){
		if(size == -1){
			try {
				byte[] bytes = stream.readAllBytes();
				byte[] tempBytes = new byte[4];
				int intsInStream = (bytes.length + 3) / 4;
				int[] ints = new int[intsInStream];
				for (int i = 0; i< intsInStream; i++){
					for (int j = 0; j<4; j++){
						if((i*4) + j<bytes.length){
							tempBytes[j] = bytes[(i*4) + j];
						} else {
							tempBytes[j] = 0;
						}
					}
					ints[i] = intFromBytes(tempBytes);
				}
				return ints;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			int[] array = new int[size];
			byte[] tempBytes = new byte[4];

			try {
				for(int i = 0; i < size; i++){
					int read = stream.read(tempBytes);

					for(int j = Math.max(0, read); j < 4; j++){
						tempBytes[i] = 0;
					}
					array[i] = intFromBytes(tempBytes);
				}
			} catch (IOException e) {
				e.printStackTrace();
				for(int i = 0; i < size; i++){
					array[i] = 0;
				}
			}
			return array;
		}
		return new int[0];
	}

	public static InputStream getFileInputStream(String path) throws FileNotFoundException {
		File file = new File(path);
		if(file.exists()){
			return new FileInputStream(file);
		}
		return null;
	}
}
