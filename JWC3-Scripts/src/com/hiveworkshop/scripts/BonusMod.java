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

public class BonusMod {
	public static void main(final String[] args) {
		final War3ObjectDataChangeset changeset = new War3ObjectDataChangeset('a');
		try {
			final boolean load = changeset.load(new File("C:/users/eric/documents/warcraft/AddingBonusMod.w3a"), null,
					false);
			final MutableObjectData manager = new MutableObjectData(WorldEditorDataType.ABILITIES,
					StandardObjectData.getStandardAbilities(), StandardObjectData.getStandardAbilityMeta(), changeset);
			for (int i = 0; i < 32; i++) {
				final int bonusModAmount = 1 << i;
				War3ID newCustomObjectId = War3ID.fromString("AX0" + (char) (i + '0'));
				if (i > 9) {
					newCustomObjectId = War3ID.fromString("AX0" + (char) (i + 'A'));
				}
				final MutableGameObject newObject = manager.createNew(newCustomObjectId, War3ID.fromString("AIlf"));
				newObject.setField(War3ID.fromString("Ilif"), 1, bonusModAmount);
				newObject.setField(War3ID.fromString("ansf"), 0, " (+" + bonusModAmount + " hp)");
			}
			try (BlizzardDataOutputStream bs = new BlizzardDataOutputStream(
					new File("C:/users/eric/documents/warcraft/AddingBonusMod_Out.w3a"))) {

				changeset.save(bs, false);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
