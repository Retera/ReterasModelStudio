package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.parsers.slk.WarcraftData;
import com.hiveworkshop.rms.parsers.w3o.Change;
import com.hiveworkshop.rms.parsers.w3o.ObjectDataChangeEntry;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MutableItemData extends MutableObjectData {

	public MutableItemData() {
		this(getStandardItems(), getStandardUnitMeta(), getWar3ObjectDataChangeset('t', "war3map.w3t"));
	}

	public MutableItemData(WarcraftData sourceSLKData,
	                       DataTable sourceSLKMetaData, War3ObjectDataChangeset editorData) {
		super(WorldEditorDataType.ITEM, sourceSLKData, sourceSLKMetaData, editorData);
	}


	public static DataTable getStandardUnitMeta() {
		return DataTableUtils.fillDataTable(new DataTable(), "Units\\UnitMetaData.slk");
	}

	public static WarcraftData getStandardItems() {
		DataTable profile = new DataTable();
		try {
			DataTableUtils.readTXT(profile, "Units\\ItemFunc.txt", true);
			DataTableUtils.readTXT(profile, "Units\\ItemStrings.txt", true);
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		DataTable itemData = DataTableUtils.fillDataTable(new DataTable(), "Units\\ItemData.slk");

		return new WarcraftData()
				.add(profile, "Profile", false)
				.add(itemData, "ItemData", true);
	}

	public void mergeChangset(War3ObjectDataChangeset changeset) {
		List<War3ID> newObjects = new ArrayList<>();
		Map<War3ID, War3ID> previousAliasToNewAlias = new HashMap<>();

		for (Map.Entry<War3ID, ObjectDataChangeEntry> entry : changeset.getCustom()) {

			War3ID nextDefaultEditorId = getNextDefaultEditorId(War3ID.fromString(entry.getKey().charAt(0) + "000"));
			System.out.println("Merging " + nextDefaultEditorId + " for  " + entry.getKey());
			// createNew API will notifier the changeNotifier
			MutableGameObject newObject = createNew(nextDefaultEditorId, entry.getValue().getOldId(), false);
			for (Map.Entry<War3ID, List<Change>> changeList : entry.getValue().getChanges()) {
				newObject.getCustomUnitData().getChanges().add(changeList.getKey(), changeList.getValue());
			}
			newObjects.add(nextDefaultEditorId);
			previousAliasToNewAlias.put(entry.getKey(), nextDefaultEditorId);
		}
		War3ID[] fieldsToCheck = getFieldsToCheck();

		for (War3ID unitId : newObjects) {
			MutableGameObject unit = get(unitId);
			fixUnitTechTreeStrings(previousAliasToNewAlias, fieldsToCheck, unit);
		}
		changeNotifier.objectsCreated(newObjects.toArray(new War3ID[0]));
	}

	public War3ObjectDataChangeset copySelectedObjects(final List<MutableGameObject> objectsToCopy) {
		War3ObjectDataChangeset changeset = new War3ObjectDataChangeset(editorData.getExpectedKind());
		Map<War3ID, War3ID> previousAliasToNewAlias = new HashMap<>();
		for (MutableGameObject gameObject : objectsToCopy) {
			ObjectDataChangeEntry gameObjectUserDataToCopy;
			ObjectDataChangeEntry gameObjectUserData;
			War3ID alias = gameObject.getAlias();
			if (editorData.getOriginal().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getOriginal().get(alias);
				War3ID startingId = War3ID.fromString(gameObject.getCode().charAt(0) + "000");
				War3ID newAlias = getNextDefaultEditorId(startingId, changeset, sourceSLKData);

				gameObjectUserData = new ObjectDataChangeEntry(gameObjectUserDataToCopy.getOldId(), newAlias);
			} else if (editorData.getCustom().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getCustom().get(alias);
				gameObjectUserData = new ObjectDataChangeEntry(gameObjectUserDataToCopy.getOldId(), gameObjectUserDataToCopy.getNewId());
			} else {
				gameObjectUserDataToCopy = null;
				War3ID startingId = War3ID.fromString(gameObject.getCode().charAt(0) + "000");
				War3ID newAlias = getNextDefaultEditorId(startingId, changeset, sourceSLKData);
				gameObjectUserData = new ObjectDataChangeEntry(gameObject.isCustom() ? gameObject.getCode() : gameObject.getAlias(), newAlias);
			}
			if (gameObjectUserDataToCopy != null) {
				for (Map.Entry<War3ID, List<Change>> changeEntry : gameObjectUserDataToCopy.getChanges()) {
					for (Change change : changeEntry.getValue()) {
						Change newChange = new Change().copyFrom(change);
						gameObjectUserData.getChanges().add(change.getId(), newChange);
					}
				}
			}
			previousAliasToNewAlias.put(gameObject.getAlias(), gameObjectUserData.getNewId());
			changeset.getCustom().put(gameObjectUserData.getNewId(), gameObjectUserData);
		}
		fixTechStuff(changeset, previousAliasToNewAlias);
		return changeset;

	}

	protected void fixTechStuff(War3ObjectDataChangeset changeset, Map<War3ID, War3ID> previousAliasToNewAlias) {
		MutableItemData changeEditManager = new MutableItemData(sourceSLKData, sourceSLKMetaData, changeset);

		War3ID[] fieldsToCheck = getFieldsToCheck();
		for (War3ID unitId : changeEditManager.keySet()) {
			MutableGameObject unit = changeEditManager.get(unitId);
			fixUnitTechTreeStrings(previousAliasToNewAlias, fieldsToCheck, unit);
		}
	}
}
