package com.hiveworkshop.modding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModJASSCompiler {
	public static void main(final String[] args) {
		if( args.length < 4 ) {
			System.err.println("Usage: <mod abbreviation> <path to Blizzard.j> <path to your war3map.j> <path to output>");
		}
		final String modName = args[0];
		final String blizzardJPath = args[1];
		final String war3mapJPath = args[2];
		final String outputPath = args[3];

		// states for state machine
		boolean inGlobals = false;
		final List<String> globals = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(war3mapJPath))) {
			// hack job here, gonna just pull strings
			String line = null;
			while( (line = reader.readLine()) != null ) {
				final String codifiedLine = codify(line);
				if( inGlobals ) {
					if( codifiedLine.equals("endglobals") ) {
						inGlobals = false;
					}
				}
				else if( codifiedLine.equals("globals") ) {
					if( globals.size() > 0 ) {
						throw new RuntimeException("Mod compilation failed: 'globals' appeared twice in file");
					}
					inGlobals = true;
				}
			}
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String codify(String line) {
		final int indexOfComment = line.indexOf("//");
		if( indexOfComment != -1  ) {
			line = line.substring(indexOfComment);
		}
		return line.trim();
	}
}
