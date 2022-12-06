package com.matrixeater.hacks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class CommentAdder {
	private static File scriptFile = new File("C:\\Users\\micro\\Downloads\\script_ids\\war3map.j");

	public static void main(final String[] args) {
		try {
			final Warcraft3MapObjectData data = Warcraft3MapObjectData.load(true);

			final File scriptFile = new File(args[0]);
			final File outFile = new File(args[1]);

			try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
					PrintWriter writer = new PrintWriter(outFile)) {
				String line;
				while ((line = reader.readLine()) != null) {
					final String startingLine = line;
					int firstIndex = line.indexOf('\'');
					final StringBuilder extraComments = new StringBuilder();
					while (firstIndex != -1) {
						final String next = line.substring(firstIndex + 1);
						final int secondIndex = next.indexOf('\'');
						if (secondIndex != -1) {
							final String id = next.substring(0, secondIndex);
							if (id.length() == 4) {
								final War3ID war3id = War3ID.fromString(id);
								MutableGameObject object = null;
								for (final MutableObjectData dataTable : data.getDatas()) {
									object = dataTable.get(war3id);
									if (object != null) {
										break;
									}
								}
								if (object != null) {
									if (extraComments.isEmpty()) {
										extraComments.append("// ");
									}
									else {
										extraComments.append(", ");
									}
									extraComments.append(id + " --> " + object.getName());
								}
							}
							line = next.substring(secondIndex + 1);
						}
						else {
							break;
						}
						firstIndex = line.indexOf('\'');
					}
					writer.println(startingLine + extraComments.toString());
				}
			}

		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
