package com.hiveworkshop.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class HexConstantExterminator {
	public static void main(final String[] args) {
		try (BufferedReader reader = new BufferedReader(new FileReader("input/UBBlizzard.j"));
				PrintWriter writer = new PrintWriter("output/UBBlizzard.j")) {
			String line;
			while ((line = reader.readLine()) != null) {
				boolean insideStringConstant = false;
				int lastDollarIndex = -1;
				for (int i = 0; i < line.length(); i++) {
					final char c = line.charAt(i);
					if (c == '"') {
						insideStringConstant = !insideStringConstant;
					} else if (c == '$' && !insideStringConstant) {
						lastDollarIndex = i;
					} else if (lastDollarIndex != -1) {
						if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F') {
							// valid
						} else {
							// invalid/end
							final int endOfLiteral = i - 1;
							if (endOfLiteral - lastDollarIndex >= 0) {
								final String toReplace = line.substring(lastDollarIndex + 1, i);
								final int numericValue = Integer.parseInt(toReplace.toLowerCase(), 16);
								final String firstPart = line.substring(0, lastDollarIndex) + numericValue;
								line = firstPart + line.substring(i);
								i = firstPart.length() - 1;
								lastDollarIndex = -1;
							}
						}
					}
				}
				if (lastDollarIndex != -1) {
					final int endOfLiteral = line.length();
					if (endOfLiteral - lastDollarIndex >= 0) {
						final String toReplace = line.substring(lastDollarIndex + 1, endOfLiteral);
						final int numericValue = Integer.parseInt(toReplace.toLowerCase(), 16);
						line = line.substring(0, lastDollarIndex) + numericValue;
						lastDollarIndex = -1;
					}
				}
				writer.println(line);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
