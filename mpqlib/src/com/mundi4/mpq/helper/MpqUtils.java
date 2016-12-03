package com.mundi4.mpq.helper;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.mundi4.mpq.StormBuffer;

public abstract class MpqUtils {

    public static int hashString(String input, int offset) {
	int seed1 = 0x7fed7fed;
	int seed2 = 0xeeeeeeee;

	for (char c : input.toCharArray()) {
	    int val = (int) Character.toUpperCase(c);
	    seed1 = StormBuffer.get(offset + val) ^ (seed1 + seed2);
	    seed2 = (val + seed1 + seed2 + (seed2 << 5) + 3);
	}
	return seed1;
    }

    public static void decryptTable(byte[] data, String key) {
	decryptBlock(data, hashString(key, 0x300));
    }

    public static void decryptBlock(byte[] data, int seed1) {
	int seed2 = 0xeeeeeeee;

	for (int i = 0; i < data.length - 3; i += 4) {
	    seed2 += StormBuffer.get(0x400 + (seed1 & 0xff));
	    int result = toInt(data, i);
	    result ^= seed1 + seed2;

	    seed1 = ((~seed1 << 21) + 0x11111111) | (seed1 >>> 11);
	    seed2 = result + seed2 + (seed2 << 5) + 3;

	    data[i + 0] = ((byte) (result & 0xff));
	    data[i + 1] = ((byte) ((result >> 8) & 0xff));
	    data[i + 2] = ((byte) ((result >> 16) & 0xff));
	    data[i + 3] = ((byte) ((result >> 24) & 0xff));
	}
    }

    public static void decryptBlock(int[] data, int seed1) {
	int seed2 = 0xeeeeeeee;

	for (int i = 0; i < data.length; i++) {
	    seed2 += StormBuffer.get(0x400 + (seed1 & 0xff));
	    int result = data[i];

	    result ^= seed1 + seed2;

	    seed1 = ((~seed1 << 21) + 0x11111111) | (seed1 >>> 11);

	    seed2 = result + seed2 + (seed2 << 5) + 3;
	    data[i] = result;
	}
    }

    public static long toUnsignedInt(int signed) {
	long result = signed;
	return result & 0xffffffffL;
    }

    public static int toUnsignedShort(short signed) {
	int result = signed;
	return result & 0xffff;
    }

    public static int readInt(InputStream in) throws IOException {
	byte[] b = new byte[4];
	if (in.read(b) != 4) {
	    throw new EOFException();
	}
	return toInt(b, 0);
    }

    public static int toInt(byte[] b, int offset) {
	int v = 0;
	for (int i = offset; i < offset + 4; i++) {
	    int n = (b[i] < 0 ? b[i] + 256 : b[i]) << (8 * i);
	    v += n;
	}
	return v;
    }

}
