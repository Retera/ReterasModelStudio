package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public class FixMySLKAgain {

	public static void main(final String[] args) {
		final File unitui = new File("C:\\Program Files (x86)\\Warcraft III Reign of Chaos Alphyra\\Units\\unitUI.slk");
		try {
			final List<String> allLines = Files.readAllLines(unitui.toPath());
			try (PrintWriter writer = new PrintWriter(unitui)) {
				final String loc = null;
				final String race = null;
				final String file = null;
				for (String line : allLines) {
					if (line.startsWith("C;X4;K")) {
						line = line + "\"";
					}
					writer.println(line);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static String trim(final String other) {
		if (other.startsWith("\"") && other.endsWith("\"") && (other.length() > 2)) {
			return other.substring(1, other.length() - 1);
		}
		return other;
	}

}
