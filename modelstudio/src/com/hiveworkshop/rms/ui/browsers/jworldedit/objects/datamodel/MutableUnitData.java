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

public final class MutableUnitData extends MutableObjectData {

//	public MutableUnitData() {
//		super(worldEditorDataType, sourceSLKData, sourceSLKMetaData, editorData);
////		this.worldEditorDataType = WorldEditorDataType.UNITS;
//////		this.sourceSLKData = StandardObjectData.getStandardUnits();
////		this.sourceSLKData = getStandardUnits();
////		resolveStringReferencesInNames(this.sourceSLKData);
//////		this.sourceSLKMetaData = StandardObjectData.getStandardUnitMeta();
////		this.sourceSLKMetaData = getStandardUnitMeta();
////		this.editorData = getWar3ObjectDataChangeset('u', "war3map.w3u");
////		metaNameToMetaId = new HashMap<>();
////		for (String metaKeyString : this.sourceSLKMetaData.keySet()) {
////			War3ID metaKey = War3ID.fromString(metaKeyString);
////			metaNameToMetaId.put(this.sourceSLKMetaData.get(metaKeyString).getField("field"), metaKey);
////		}
////		cachedKeyToGameObject = new HashMap<>();
////		changeNotifier = new MutableObjectDataChangeNotifier();
//	}

	public MutableUnitData() {
		this(getStandardUnits(), getStandardUnitMeta(), getWar3ObjectDataChangeset('u', "war3map.w3u"));
//		super(WorldEditorDataType.UNITS, getStandardUnits(), getStandardUnitMeta(), editorData);
//		super(worldEditorDataType, sourceSLKData, sourceSLKMetaData, editorData);
//		this.worldEditorDataType = WorldEditorDataType.UNITS;
//		this.sourceSLKData = sourceSLKData;
//		resolveStringReferencesInNames(this.sourceSLKData);
//		this.sourceSLKMetaData = sourceSLKMetaData;
//		this.editorData = editorData;
//		metaNameToMetaId = new HashMap<>();
//		for (String metaKeyString : this.sourceSLKMetaData.keySet()) {
//			War3ID metaKey = War3ID.fromString(metaKeyString);
//			metaNameToMetaId.put(this.sourceSLKMetaData.get(metaKeyString).getField("field"), metaKey);
//		}
//		cachedKeyToGameObject = new HashMap<>();
//		changeNotifier = new MutableObjectDataChangeNotifier();
	}

	public MutableUnitData(WarcraftData sourceSLKData,
	                       DataTable sourceSLKMetaData, War3ObjectDataChangeset editorData) {
		super(WorldEditorDataType.UNITS, sourceSLKData, sourceSLKMetaData, editorData);
//		super(worldEditorDataType, sourceSLKData, sourceSLKMetaData, editorData);
//		this.worldEditorDataType = WorldEditorDataType.UNITS;
//		this.sourceSLKData = sourceSLKData;
//		resolveStringReferencesInNames(this.sourceSLKData);
//		this.sourceSLKMetaData = sourceSLKMetaData;
//		this.editorData = editorData;
//		metaNameToMetaId = new HashMap<>();
//		for (String metaKeyString : this.sourceSLKMetaData.keySet()) {
//			War3ID metaKey = War3ID.fromString(metaKeyString);
//			metaNameToMetaId.put(this.sourceSLKMetaData.get(metaKeyString).getField("field"), metaKey);
//		}
//		cachedKeyToGameObject = new HashMap<>();
//		changeNotifier = new MutableObjectDataChangeNotifier();
	}

	public static DataTable getStandardUnitMeta() {
		return DataTableUtils.getDataTable("Units\\UnitMetaData.slk");
	}

	public static WarcraftData getStandardUnits() {

		final DataTable profile = new DataTable();

		try {
//			DataTableUtils.readTXT(profile, "Units\\CampaignUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\CampaignUnitStrings.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\HumanUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\HumanUnitStrings.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\NeutralUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\NeutralUnitStrings.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\NightElfUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\NightElfUnitStrings.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\OrcUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\OrcUnitStrings.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\UndeadUnitFunc.txt", true);
//			DataTableUtils.readTXT(profile, "Units\\UndeadUnitStrings.txt", true);

			String[] standardUnitPaths = {
					"Units\\CampaignUnitFunc.txt",
					"Units\\CampaignUnitStrings.txt",
					"Units\\HumanUnitFunc.txt",
					"Units\\HumanUnitStrings.txt",
					"Units\\NeutralUnitFunc.txt",
					"Units\\NeutralUnitStrings.txt",
					"Units\\NightElfUnitFunc.txt",
					"Units\\NightElfUnitStrings.txt",
					"Units\\OrcUnitFunc.txt",
					"Units\\OrcUnitStrings.txt",
					"Units\\UndeadUnitFunc.txt",
					"Units\\UndeadUnitStrings.txt"};
			for (String path : standardUnitPaths) {
				DataTableUtils.readTXT(profile, path, true);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}


		DataTable unitAbilities = DataTableUtils.getDataTable("Units\\UnitAbilities.slk");
		DataTable unitBalance = DataTableUtils.getDataTable("Units\\UnitBalance.slk");
		DataTable unitData = DataTableUtils.getDataTable("Units\\UnitData.slk");
		DataTable unitUI = DataTableUtils.getDataTable("Units\\UnitUI.slk");
		DataTable unitWeapons = DataTableUtils.getDataTable("Units\\UnitWeapons.slk");

		final DataTable skin = new DataTable();
		try {
//			final CompoundDataSource source = GameDataFileSystem.getDefault();
			DataTableUtils.readTXT(skin, "Units\\UnitSkin.txt", true);
//			final InputStream unitSkin = GameDataFileSystem.getDefault().getResourceAsStream("Units\\UnitSkin.txt");
//			if (unitSkin != null) {
//				DataTableUtils.readTXT(skin, unitSkin, true);
//			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		WarcraftData units = new WarcraftData();

		units.add(profile, "Profile", false);
		units.add(unitAbilities, "UnitAbilities", true);
		units.add(unitBalance, "UnitBalance", true);
		units.add(unitData, "UnitData", true);
		units.add(unitUI, "UnitUI", true);
		units.add(unitWeapons, "UnitWeapons", true);
		// TODO: The actual War3 game engine does not use this string, "ProfileSkin",
		//  it appears that their architecture for handling this data is quite different.
		//  They give the skin data a lower load priority than UnitUI, which has a lower
		//  load priority than old profile data. However, they still use the
		//  string "Profile" for the skin data. By putting the invented string
		//  "ProfileSkin" here, my custom object editor will be unable to modify skin
		//  data until further notice. But the model studio will work nicely with the
		//  data being formatted visually the same as the game.
		units.add(skin, "ProfileSkin", false);

		return units;
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

	protected void fixUnitTechTreeStrings(Map<War3ID, War3ID> previousAliasToNewAlias, War3ID[] fieldsToCheck, MutableGameObject unit) {
		for (War3ID field : fieldsToCheck) {
			String techTreeString = unit.getFieldAsString(field, 0);
			List<String> resultingTechList = getTechList(previousAliasToNewAlias, techTreeString);
			StringBuilder sb = new StringBuilder();
			for (String tech : resultingTechList) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(tech);
			}
			unit.setField(field, 0, sb.toString());
		}
	}

	protected List<String> getTechList(Map<War3ID, War3ID> previousAliasToNewAlias, String techtreeString) {
		String[] techList = techtreeString.split(",");
		List<String> resultingTechList = new ArrayList<>();
		for (String tech : techList) {
			if (tech.length() == 4) {
				War3ID newTechId = previousAliasToNewAlias.get(War3ID.fromString(tech));
				if (newTechId != null) {
					resultingTechList.add(newTechId.toString());
				} else {
					resultingTechList.add(tech);
				}
			} else {
				resultingTechList.add(tech);
			}
		}
		return resultingTechList;
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
		MutableUnitData changeEditManager = new MutableUnitData(sourceSLKData, sourceSLKMetaData, changeset);

		War3ID[] fieldsToCheck = getFieldsToCheck();
		for (War3ID unitId : changeEditManager.keySet()) {
			MutableGameObject unit = changeEditManager.get(unitId);
			fixUnitTechTreeStrings(previousAliasToNewAlias, fieldsToCheck, unit);
		}
	}

	protected War3ID[] getFieldsToCheck() {
		return new War3ID[] {War3ID.fromString("utra"), War3ID.fromString("uupt"), War3ID.fromString("ubui")};
	}
}
