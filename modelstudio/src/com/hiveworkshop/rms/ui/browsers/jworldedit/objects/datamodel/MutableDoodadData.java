package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.DataTableUtils;
import com.hiveworkshop.rms.parsers.slk.WarcraftData;
import com.hiveworkshop.rms.parsers.w3o.Change;
import com.hiveworkshop.rms.parsers.w3o.ObjectDataChangeEntry;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.util.War3ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MutableDoodadData extends MutableObjectData {

	public MutableDoodadData() {
		this(getStandardDoodads(), getStandardDoodadMeta(), getWar3ObjectDataChangeset('d', "war3map.w3d"));
	}

	public MutableDoodadData(WarcraftData sourceSLKData,
	                         DataTable sourceSLKMetaData, War3ObjectDataChangeset editorData) {
		super(WorldEditorDataType.DOODADS, sourceSLKData, sourceSLKMetaData, editorData);
	}

	public static WarcraftData getStandardDoodads() {
		DataTable doodadData = DataTableUtils.fillDataTable(new DataTable(), "Doodads\\Doodads.slk");
		final DataTable profile = new DataTable();
		try {
			DataTableUtils.readTXT(profile, "Doodads\\DoodadSkins.txt", true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new WarcraftData()
				.add(profile, "Profile", true)
				.add(doodadData, "DoodadData", true);
	}

	public static DataTable getStandardDoodadMeta() {
		return DataTableUtils.fillDataTable(new DataTable(), "Doodads\\DoodadMetaData.slk");
	}

	// TODO remove this hack
	public War3ObjectDataChangeset getEditorData() {
		return editorData;
	}

	public Map<String, War3ID> getMetaNameToMetaId() {
		return metaNameToMetaId;
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
			War3ID alias = gameObject.getAlias();

			War3ID oldId;
			War3ID newId;
			if (editorData.getOriginal().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getOriginal().get(alias);

				oldId = gameObjectUserDataToCopy.getOldId();
				War3ID startingId = War3ID.fromString(gameObject.getCode().charAt(0) + "000");
				newId = getNextDefaultEditorId(startingId, changeset, sourceSLKData);
			} else if (editorData.getCustom().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getCustom().get(alias);

				oldId = gameObjectUserDataToCopy.getOldId();
				newId = gameObjectUserDataToCopy.getNewId();
			} else {
				gameObjectUserDataToCopy = null;

				oldId = gameObject.isCustom() ? gameObject.getCode() : gameObject.getAlias();
				War3ID startingId = War3ID.fromString(gameObject.getCode().charAt(0) + "000");
				newId = getNextDefaultEditorId(startingId, changeset, sourceSLKData);
			}
			ObjectDataChangeEntry gameObjectUserData = new ObjectDataChangeEntry(oldId, newId);

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
		MutableDoodadData changeEditManager = new MutableDoodadData(sourceSLKData, sourceSLKMetaData, changeset);

		War3ID[] fieldsToCheck = getFieldsToCheck();
		for (War3ID unitId : changeEditManager.keySet()) {
			MutableGameObject unit = changeEditManager.get(unitId);
			fixUnitTechTreeStrings(previousAliasToNewAlias, fieldsToCheck, unit);
		}
	}

}
