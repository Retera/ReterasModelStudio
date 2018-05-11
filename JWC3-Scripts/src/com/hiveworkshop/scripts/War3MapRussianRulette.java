package com.hiveworkshop.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class War3MapRussianRulette {

	public static void main(final String[] args) {
		final File maps = new File("C:/users/eric/documents/Warcraft III/Maps");
		final File[] listFiles = maps.listFiles();
		final int random = (int) (Math.random() * listFiles.length);
		final File randomMap = listFiles[random];
		folders(randomMap);
	}

	public static void folders(final File file) {
		if (file.isDirectory()) {
			final File[] listFiles = file.listFiles();
			final int random = (int) (Math.random() * listFiles.length);
			final File randomMap = listFiles[random];
			folders(randomMap);
		} else {
			final int numberOfPlayers = getNumberOfPlayers(file);
			if (numberOfPlayers == 1) {
				folders(file.getParentFile());
			} else {
				System.out.println(file);
				System.out.println(numberOfPlayers);
			}
		}
	}

	public static int getNumberOfPlayers(final File file) {
		try (InputStream is = new FileInputStream(file)) {
			final byte[] one = new byte[1];
			int consecutiveNullBytes = 0;
			int programWideOffset = 0;
			boolean sawExactlyOne = false;
			boolean sawExactlyTwoAfter = false;
			while (is.read(one) != -1) {
				if (one[0] == 0) {
					consecutiveNullBytes++;
				} else {
					if (consecutiveNullBytes == 1) {
						sawExactlyOne = true;
					} else if (consecutiveNullBytes == 2 && sawExactlyOne) {
						sawExactlyTwoAfter = true;
						return one[0];
					} else if (sawExactlyTwoAfter) {
						return one[0];
					}
					consecutiveNullBytes = 0;
				}
				programWideOffset++;
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
