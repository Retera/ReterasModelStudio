package com.hiveworkshop.wc3.gui.datachooser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC.FileSystem;

public class GetCascLanguageList {
	public static void main(final String[] args) {
		try {
			final WarcraftIIICASC casc = new WarcraftIIICASC(Paths.get("C:/Program Files/Warcraft III"), true);
			final FileSystem rootFileSystem = casc.getRootFileSystem();
			if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
				final ByteBuffer buffer = rootFileSystem.readFileData("index");
				final Set<String> categories = new HashSet<>();
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new ByteArrayInputStream(buffer.array())))) {
					String line;
					while ((line = reader.readLine()) != null) {
						final String[] splitLine = line.split("\\|");
						if (splitLine.length >= 3) {
							final String category = splitLine[2];
							categories.add(category);
						}
					}
				}
				for (final String category : categories) {
					System.out.println(category);
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
