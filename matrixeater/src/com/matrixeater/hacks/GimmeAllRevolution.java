package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class GimmeAllRevolution {
	public static void main(final String[] args) {
		try {
			final Warcraft3MapObjectData data = Warcraft3MapObjectData.load(true);
			final MutableObjectData units = data.getUnits();
			for (final War3ID unitId : units.keySet()) {
				final MutableGameObject unitType = units.get(unitId);
				final String file = convertPathToMDX(unitType.getFieldAsString(War3ID.fromString("umdl"), 0));
				final InputStream stream = MpqCodebase.get().getResourceAsStream(file);
				final Path target = Paths.get("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Revolution",
						file);
				Files.createDirectories(target.getParent());
				if (stream != null) {
					Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}
}
