package com.matrixeater.hacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RegenErrors {

	public static void main(final String[] args) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(RegenErrors.class.getResourceAsStream("errors.txt")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				final String replace = line.replace("msquish", "squish");
				System.out.println("Source then target:");
				System.out.println(replace);
				System.out.println(line);
				Files.copy(Paths.get(replace), Paths.get(line), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
