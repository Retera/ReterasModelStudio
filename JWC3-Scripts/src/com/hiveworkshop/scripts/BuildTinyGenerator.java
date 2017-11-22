package com.hiveworkshop.scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.etheller.collections.List;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.objectdata.Change;
import com.hiveworkshop.wc3.units.objectdata.ObjectDataChangeEntry;
import com.hiveworkshop.wc3.units.objectdata.WTSFile;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;
import mpq.MPQException;

public class BuildTinyGenerator {

	private static final War3ID BUILD_TINY_ID = War3ID.fromString("AIbh");

	public static void main(final String[] args) {
		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			final LoadedMPQ loadMPQ = mpqCodebase.loadMPQ(Paths.get("input/Demons and Wizards.w3x"));
			final InputStream resourceAsStream = mpqCodebase.getResourceAsStream("war3map.w3u");
			final InputStream resourceAsStream1 = mpqCodebase.getResourceAsStream("war3map.w3u");
			Files.copy(resourceAsStream1, Paths.get("output/war3map_orig.w3u"), StandardCopyOption.REPLACE_EXISTING);
			final War3ObjectDataChangeset obj = new War3ObjectDataChangeset();
			final War3ObjectDataChangeset abilities = new War3ObjectDataChangeset('a');
			final WTSFile wts = new WTSFile(mpqCodebase.getResourceAsStream("war3map.wts"));
			if (!obj.load(new BlizzardDataInputStream(resourceAsStream), wts, true)) {
				System.err.println("Failed to parse.");
			}
			abilities.load(new BlizzardDataInputStream(mpqCodebase.getResourceAsStream("war3map.w3a")), wts, true);
			final WarcraftData standardUnits = StandardObjectData.getStandardUnits();
			War3ID baseAbilityId = War3ID.fromString("A000");
			for (final MapView.Entry<War3ID, ObjectDataChangeEntry> unitEntry : obj.getCustom()) {
				final List<Change> isBuildingChangelist = unitEntry.getValue().getChanges()
						.get(War3ID.fromString("ubdg"));
				final GameObject parentGameDataUnit = standardUnits.get(unitEntry.getValue().getOldId().toString());
				if ((isBuildingChangelist != null && isBuildingChangelist.size() > 0
						&& isBuildingChangelist.get(0).isBoolval())
						|| (parentGameDataUnit != null && parentGameDataUnit.getFieldValue("isbldg") == 1)) {
					// then it's a building
					baseAbilityId = getNextAbilityId(abilities, baseAbilityId);
					final ObjectDataChangeEntry newAbility = new ObjectDataChangeEntry(BUILD_TINY_ID, baseAbilityId);
					final Change buildUnitId = new Change();
					final War3ID buildUnitIdMetaType = War3ID.fromString("Ibl1");
					buildUnitId.setId(buildUnitIdMetaType);
					buildUnitId.setLevel(1);
					buildUnitId.setVartype(3);
					final String buildingIdString = unitEntry.getKey().toString();
					buildUnitId.setStrval(buildingIdString + "," + buildingIdString + "," + buildingIdString + ","
							+ buildingIdString);
					newAbility.getChanges().add(buildUnitIdMetaType, buildUnitId);

					final Change buildTinyNameChange = new Change();
					final War3ID abilityNameMetaType = War3ID.fromString("anam");
					buildTinyNameChange.setId(abilityNameMetaType);
					buildTinyNameChange.setVartype(3);
					final String nameValue;

					final List<Change> unitNameChanges = unitEntry.getValue().getChanges().get(obj.getNameField());
					if (unitNameChanges != null && unitNameChanges.size() > 0) {
						nameValue = unitNameChanges.get(0).getStrval();
					} else {
						nameValue = parentGameDataUnit.getField("Name");
					}
					buildTinyNameChange.setStrval("Build Tiny " + nameValue);
					newAbility.getChanges().add(abilityNameMetaType, buildTinyNameChange);

					final List<Change> unitIconChanges = unitEntry.getValue().getChanges()
							.get(War3ID.fromString("uico"));
					String iconValue;
					if (unitIconChanges != null && unitIconChanges.size() > 0) {
						iconValue = unitIconChanges.get(0).getStrval();
					} else {
						iconValue = parentGameDataUnit.getField("Art");
					}
					final Change buildTinyIconChange = new Change();
					final War3ID abilityIconMetaType = War3ID.fromString("aart");
					buildTinyIconChange.setId(abilityIconMetaType);
					buildTinyIconChange.setVartype(3);
					buildTinyIconChange.setStrval(iconValue);
					newAbility.getChanges().add(abilityIconMetaType, buildTinyIconChange);

					abilities.getCustom().put(baseAbilityId, newAbility);
				}
			}

			// final Obj myTable = loadtable("my_out_test.w3a", wts);
			// final Obj cureTable = loadtable("cure6.w3a", wts);
			// for (final MapView.Entry<War3ID, ObjectDataChangeEntry> entry : myTable.getCustom()) {
			// if (!cureTable.getCustom().containsKey(entry.getKey())) {
			// System.out.println("FOUND MISSING: " + entry.getKey());
			// }
			// }
			abilities.save(new BlizzardDataOutputStream(new File("output/tinies.w3a")), false);
		} catch (final MPQException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static War3ID getNextAbilityId(final War3ObjectDataChangeset abilities, War3ID baseAbilityId) {
		while (abilities.getCustom().containsKey(baseAbilityId) || !(Character.isDigit(baseAbilityId.charAt(3))
				|| (baseAbilityId.charAt(3) >= 'A' && baseAbilityId.charAt(3) <= 'Z'))) {
			baseAbilityId = new War3ID(baseAbilityId.getValue() + 1);
		}
		return baseAbilityId;
	}

}
