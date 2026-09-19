package com.hiveworkshop.wc3.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.hiveworkshop.wc3.pkb.PkbEffect;
import com.hiveworkshop.wc3.pkb.PkbObject;
import com.hiveworkshop.wc3.pkb.PkbReader;
import com.hiveworkshop.wc3.pkb.PkbValue;

/**
 * Prints the object graph of one or more .pkb files. Usage:
 * {@code PkbDump [--brief] file.pkb ...}
 */
public final class PkbDump {
	private PkbDump() {
	}

	public static void main(final String[] args) throws IOException {
		boolean brief = false;
		for (final String arg : args) {
			if (arg.equals("--brief")) {
				brief = true;
				continue;
			}
			final PkbEffect effect = PkbReader.read(Files.readAllBytes(Paths.get(arg)));
			System.out.println("== " + arg + " v" + effect.versionMajor + "." + effect.versionMinor + "."
					+ effect.versionPatch + " rev " + effect.revisionId + " generator " + effect.generator + " "
					+ effect.getObjects().size() + " objects, " + effect.getStrings().size() + " strings");
			for (final PkbObject object : effect.getObjects()) {
				System.out.println(object);
				if (brief) {
					continue;
				}
				for (final Map.Entry<String, PkbValue> field : object.getFields().entrySet()) {
					String text = field.getValue().toString();
					if (text.length() > 200) {
						text = text.substring(0, 200) + "...";
					}
					System.out.println("    " + field.getKey() + " (" + field.getValue().type + ") = " + text);
				}
			}
		}
	}
}
