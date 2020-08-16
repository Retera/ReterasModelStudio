package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class GetMeTheHeroSkills {
	private static final War3ID ACQR = War3ID.fromString("uacq");
	private static final War3ID UA1R = War3ID.fromString("ua1r");
	private static final War3ID UA2R = War3ID.fromString("ua2r");
	private static final War3ID UA1W = War3ID.fromString("ua1w");
	private static final War3ID UA2W = War3ID.fromString("ua2w");

	public static void main(final String[] args) {
		final War3ObjectDataChangeset customUnitChanges = new War3ObjectDataChangeset('u');
		try (BlizzardDataInputStream blizData = new BlizzardDataInputStream(
				new FileInputStream(new File("C:\\Temp\\ud.w3u")))) {
			customUnitChanges.load(blizData, null, false);

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final WarcraftData standardUnits = StandardObjectData.getStandardUnits();
		final DataTable standardUnitMeta = StandardObjectData.getStandardUnitMeta();
		final MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, standardUnits,
				standardUnitMeta, customUnitChanges);

		for (final War3ID unitID : unitData.keySet()) {
			// for each unit that exists
			final MutableGameObject unit = unitData.get(unitID);
			if (customUnitChanges.getCustom().containsKey(unitID)
					|| customUnitChanges.getOriginal().containsKey(unitID)) {
				final float attack1Range = unit.getFieldAsInteger(UA1R, 0);
				final float attack2Range = unit.getFieldAsInteger(UA2R, 0);
				final float ultimateRangeValue = Math.max(attack1Range, attack2Range);

				if (ultimateRangeValue >= 500) {
					unit.setField(ACQR, 0, ultimateRangeValue);
				}
			}
		}

		try (BlizzardDataOutputStream outputStream = new BlizzardDataOutputStream(new File("C:\\Temp\\Output.w3u"))) {
			customUnitChanges.save(outputStream, false);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

}
