package com.hiveworkshop.scripts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.etheller.collections.HashMap;
import com.etheller.collections.List;
import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.objectdata.Change;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.ObjectDataChangeEntry;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;

public final class EditEverythingFieldDetector {
	public static void main(final String[] args) {
		final War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('u');
		final WarcraftData standardUnitData = StandardObjectData.getStandardUnits();
		final DataTable standardUnitMetaData = StandardObjectData.getStandardUnitMeta();
		final MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS, standardUnitData,
				standardUnitMetaData, editorData);
		try {
			editorData.load(new BlizzardDataInputStream(new FileInputStream("input/editEverything.w3u")), null, false);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final Map<String, Integer> typeNameToTypeId = new HashMap<>();
		final ObjectDataChangeEntry peasant = editorData.getOriginal().get(War3ID.fromString("hpea"));
		for (final Map.Entry<War3ID, List<Change>> entry : peasant.getChanges()) {
			final War3ID key = entry.getKey();
			final List<Change> value = entry.getValue();
			final Change change = value.get(0);
			final String metaType = standardUnitMetaData.get(key.toString()).getField("type");
			typeNameToTypeId.put(metaType, change.getVartype());
		}
		for (final MapView.Entry<String, Integer> entry : typeNameToTypeId) {
			String idBasedName = "unknown";
			switch (entry.getValue()) {
			case War3ObjectDataChangeset.VAR_TYPE_BOOLEAN:
				idBasedName = "bool";
				break;
			case War3ObjectDataChangeset.VAR_TYPE_INT:
				idBasedName = "int";
				break;
			case War3ObjectDataChangeset.VAR_TYPE_REAL:
				idBasedName = "real";
				break;
			case War3ObjectDataChangeset.VAR_TYPE_STRING:
				idBasedName = "string";
				break;
			case War3ObjectDataChangeset.VAR_TYPE_UNREAL:
				idBasedName = "unreal";
				break;
			}
			System.out.println(entry.getKey() + ": " + idBasedName);
		}

	}
}
