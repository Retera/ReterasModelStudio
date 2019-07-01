package com.hiveworkshop.wc3.user;

public class TestMain {

	public static void main(final String[] args) {
		final String readRegistry = WindowsRegistry
				.readRegistry("HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Classic Launcher\\w3", "Locale");
		System.out.println(readRegistry);
	}

}
