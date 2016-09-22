package com.mundi4.mpq;

public final class StormBuffer {

    private static final int[] buf;

    private StormBuffer() {
    }

    static {
	buf = new int[0x500];
	int seed = 0x100001;
	for (int i = 0; i < 0x100; i++) {
	    int index = i;
	    for (int j = 0; j < 5; j++, index += 0x100) {
		seed = (seed * 125 + 3) % 0x2aaaab;
		int temp = (seed & 0xffff) << 16;
		seed = (seed * 125 + 3) % 0x2aaaab;
		buf[index] = temp | (seed & 0xffff);
	    }
	}
    }

    public static final int get(int i) {
	return buf[i];
    }

}
