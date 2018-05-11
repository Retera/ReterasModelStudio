package com.hiveworkshop.scripts;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataOutputStream;

public class BonusMod2 {
	public static void main(final String[] args) {
		final War3ObjectDataChangeset changeset = new War3ObjectDataChangeset('u');
		try {
			final boolean load = changeset.load(new File("C:/users/eric/documents/warcraft/maps/TestData.w3u"), null,
					false);
			final MutableObjectData manager = new MutableObjectData(WorldEditorDataType.UNITS,
					StandardObjectData.getStandardUnits(), StandardObjectData.getStandardUnitMeta(), changeset);
			for (int i = 0; i < 32; i++) {
				War3ID newCustomObjectId = War3ID.fromString("hX0" + (char) (i + '0'));
				if (i > 9) {
					newCustomObjectId = War3ID.fromString("hX0" + (char) ((i - 9) + 'A'));
				}
				final MutableGameObject newObject = manager.createNew(newCustomObjectId, War3ID.fromString("hfoo"));
				newObject.setField(War3ID.fromString("unam"), 0, "This is a test " + i);
				newObject.setField(War3ID.fromString("ubba"), 0, 30 + i * 8);
			}
			try (BlizzardDataOutputStream bs = new BlizzardDataOutputStream(
					new File("C:/users/eric/documents/warcraft/maps/TestData_Out.w3u"))) {

				changeset.save(bs, false);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
