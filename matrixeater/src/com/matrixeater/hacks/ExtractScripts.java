package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class ExtractScripts {
	public static void main(final String[] args) {
		final InputStream scriptings = MpqCodebase.get().getResourceAsStream("war3map.lua");
		try {
			Files.copy(scriptings, Paths.get("C:/temp/war3map.lua"));
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
