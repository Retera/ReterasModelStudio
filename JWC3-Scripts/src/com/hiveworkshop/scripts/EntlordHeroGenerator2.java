package com.hiveworkshop.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public final class EntlordHeroGenerator2 {
	public static void main(final String[] args) {
		final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
		final WarcraftData standardUnitData = StandardObjectData.getStandardUnits();
		final DataTable standardUnitMetaData = StandardObjectData.getStandardUnitMeta();
		final MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, standardUnitData,
				standardUnitMetaData, editorData);
		try {
			editorData.load(new BlizzardDataInputStream(new FileInputStream("input/dochero.w3u")), null, false);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final MutableGameObject reterasNewHero = unitData.get(War3ID.fromString("Nngs"));
		System.out.println(reterasNewHero.getFieldAsString(War3ID.fromString("umdl"), 0));

		final MutableGameObject ancientOfWar = unitData.get(War3ID.fromString("etrp"));
		for (final String key : standardUnitMetaData.keySet()) {
			final Element metaDataField = standardUnitMetaData.get(key);
			if (metaDataField.getField("useHero").equals("1") || metaDataField.getField("useUnit").equals("1")
					|| metaDataField.getField("useBuilding").equals("1")) {
				final String metaDataType = metaDataField.getField("type");
				switch (metaDataType) {
				case "attackBits":
				case "teamColor":
				case "deathType":
				case "versionFlags":
				case "int":
				case "unint":
				case "bool":
					reterasNewHero.setField(War3ID.fromString(key), 0,
							ancientOfWar.getFieldAsInteger(War3ID.fromString(key), 0));
					break;
				case "real":
				case "unreal":
					reterasNewHero.setField(War3ID.fromString(key), 0,
							ancientOfWar.getFieldAsFloat(War3ID.fromString(key), 0));
					break;
				// reterasNewHero.setField(War3ID.fromString(key), 0,
				// ancientOfWar.getFieldAsBoolean(War3ID.fromString(key), 0));
				// break;
				default:
					System.err.println("default case for type: " + key + ", name: " + metaDataField.getField("field")
							+ ", type: " + metaDataType);
				case "string":
					reterasNewHero.setField(War3ID.fromString(key), 0,
							ancientOfWar.getFieldAsString(War3ID.fromString(key), 0));
					break;
				}
			}
		}
		try {
			try (BlizzardDataOutputStream outStream = new BlizzardDataOutputStream(new File("output/dochero.w3u"))) {
				editorData.save(outStream, false);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}
}
