package com.matrixeater.hacks.jass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ReturnBugModernizer {

	public static void main(final String[] args) {
		final File file = new File(args[0]);

		final Map<String, String> nameToType = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("takes")) {
					System.out.println(line);

				}
			}
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ReturnBugModernizer() {

	}
}
