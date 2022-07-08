package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.util.BinaryReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BinaryDecipherHelper {
	// For testing purposes

	public static void load(final File in){
		try {
			load(new FileInputStream(in));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void load(InputStream inputStream) throws IOException {
		load(ByteBuffer.wrap(inputStream.readAllBytes()));
	}


	public static void load(final ByteBuffer buffer) {
//		System.out.println("" + MathUtils.uint8ToUint32((byte) 0, (byte) 255, (byte) 0, (byte) 255));
		final BinaryReader reader = new BinaryReader(buffer);


//		int LAYS = ('L' << 24) | ('A' << 16) | ('Y' << 8) | ('S');
//		System.out.println("LAYS = " + LAYS);
//		System.out.println("VERS = " + VERS
//				+ "\nMODL = " + MODL
//				+ "\nSEQS = " + SEQS
//				+ "\nGLBS = " + GLBS
//				+ "\nMTLS = " + MTLS
//				+ "\nTEXS = " + TEXS
//				+ "\nTXAN = " + TXAN
//				+ "\nGEOS = " + GEOS
//				+ "\nGEOA = " + GEOA
//				+ "\nBONE = " + BONE
//				+ "\nLITE = " + LITE
//				+ "\nHELP = " + HELP
//				+ "\nATCH = " + ATCH
//				+ "\nPIVT = " + PIVT
//				+ "\nPREM = " + PREM
//				+ "\nPRE2 = " + PRE2
//				+ "\nCORN = " + CORN
//				+ "\nRIBB = " + RIBB
//				+ "\nCAMS = " + CAMS
//				+ "\nEVTS = " + EVTS
//				+ "\nCLID = " + CLID
//				+ "\nFAFX = " + FAFX
//				+ "\nBPOS = " + BPOS);

		System.out.println("readers remaining: " + reader.remaining());
		while (reader.remaining() > 0) {
//			System.out.println(reader.readTag());
//			System.out.println(reader.readInt32());
//			System.out.println(reader.readFloat32());
//			System.out.println(reader.read(8));
//			System.out.println(read2(buffer, 4));
//			System.out.println(read(buffer, 4));
//			System.out.println(readHexRGBA(buffer));
//			System.out.println(readBytes(buffer, 4));
			System.out.println(readAndTryMultipleDecodings(buffer, 4));
		}

//		if (reader.readTag() != MDLX) {
//			throw new IllegalStateException("WrongMagicNumber");
//		}
//		try {
//			System.out.println("bufferSize: " + reader.remaining());
//			while (reader.remaining() > 0) {
//				int tag = 0;
//				int size = 0;
//				int mode = 0;
//				try {
//					mode = 0;
//					tag = reader.readTag();
//					mode = 1;
//					size = reader.readInt32();
//					mode = 2;
//					System.out.println("tag: " + new War3ID(tag));
//
//					lodByTag(mdlxModel, reader, tag, size);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static String read(ByteBuffer buffer, final int count) {
		StringBuilder value = new StringBuilder();
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; i++) {
			byte b = buffer.get();
			bytes[i] = b;
//			if (b != 0) {
//			}
			value.append((char) (b & 0xFF));
		}

		return value.toString();
	}
	public static String readAndTryAsString(ByteBuffer buffer, final int count) {
		StringBuilder value = new StringBuilder();
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; i++) {
			byte b = buffer.get();
			bytes[i] = b;
//			if (b != 0) {
//			}
			value.append((char) (b & 0xFF));
		}


		String string = value.toString();
		if(string.matches("\\w*")){
			return string;
		} else {
			int num = 0;
			for (int i = 0; i < count; i++){
				num += bytes[i] << (i * 8);
			}
			return "" + num;
		}
	}

	public static String readBytes(ByteBuffer buffer, final int count) {
		StringBuilder value = new StringBuilder();
//		byte[] bytes = new byte[count];
		for (int i = 0; i < count; i++) {
			byte b = buffer.get();
//			bytes[i] = b;
//			if (b != 0) {
//			}
			value.append(b).append(" ");
		}


		return value.toString();
	}
	public static String readAndTryMultipleDecodings(ByteBuffer buffer, final int count) {
		StringBuilder value = new StringBuilder();
		int num = 0;
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; i++) {
			byte b = buffer.get();
			bytes[i] = b;
//			if (b != 0) {
//			}
			value.append((char) (b & 0xFF));
			num += bytes[i] << (i * 8);
		}
		value.append("\t").append(num).append("\t").append(Float.intBitsToFloat(num)).append("\t");

		int s1 = (num >> 16) & 0xFFFF;
		int s2 = num & 0xFFFF;
		value.append(s1).append(" ").append(s2).append("    \t");

//		for (int i = 3; i >= 0; i--) {
//			value.append(Integer.toHexString((num >> i*8) & 0xFF)).append(" ");
//		}
		for (byte b : bytes){
			value.append(b).append(" ");
		}

		return value.toString();
	}

	public int readTag(ByteBuffer buffer) {
		return Integer.reverseBytes(readInt32(buffer));
	}

	public int readInt32(ByteBuffer buffer) {
		return buffer.getInt();
	}
	public static String readHexRGBA(ByteBuffer buffer) {
		int anInt = buffer.getInt();
		StringBuilder value = new StringBuilder();
//		int ugg =  ((b32 << 24) & 0xFF000000) | ((b24 << 16) & 0xFF0000) | ((b16 << 8) & 0xFF00) | (b8 & 0xFF);
		for (int i = 3; i >= 0; i--) {

			value.append(Integer.toHexString((anInt >> i*8) & 0xFF)).append(" ");
		}

		return value.toString();
	}
}
