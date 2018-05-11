package com.hiveworkshop.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;

import mpq.MPQException;

public class Revolution {
	public static void main(final String[] args) {
		try {
			final LoadedMPQ loadMPQ = MpqCodebase.get().loadMPQ(Paths.get("I:/PRSCMOD/Revolution.mpq"));
		} catch (final MPQException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		try (InputStream inputStream = MpqCodebase.get().getResourceAsStream("Scripts\\blizzard.j")) {
			Files.copy(inputStream, Paths.get("output/RevBlizzard.j"), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
