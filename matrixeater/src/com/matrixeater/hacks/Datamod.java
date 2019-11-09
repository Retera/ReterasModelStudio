package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import de.wc3data.stream.BlizzardDataOutputStream;

public class Datamod {
	private static final War3ID GLVL = War3ID.fromString("glvl");
	private static final War3ID GLMM = War3ID.fromString("glmm");
	private static final War3ID GGLM = War3ID.fromString("gglm");

	public static void main(final String[] args) {
		Warcraft3MapObjectData data;
		try {
			data = Warcraft3MapObjectData.load(false);
			final MutableObjectData upgrades = data.getUpgrades();
			for (final War3ID upgradeID : upgrades.keySet()) {
				final MutableGameObject upgrade = upgrades.get(upgradeID);
				final int upgradeMaximumLevels = upgrade.getFieldAsInteger(GLVL, 0);
				if (upgradeMaximumLevels > 1) {
					upgrade.setField(GLVL, 0, 256);
					upgrade.setField(GLMM, 0, 0);
					upgrade.setField(GGLM, 0, 0);
				}
			}
			try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(new File("upgrades.w3q"))) {
				upgrades.getEditorData().save(outputStream, false);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
