package com.hiveworkshop.scripts;

public class bitsgoof {

	public static void main(final String[] args) {
		final int value = ((0x36FF22FF | 0x3AB3AEE9) >> (0x01)) & 0x1010FFFF;
		System.out.println(value);
		System.out.println(Integer.toHexString(value));
	}

}
