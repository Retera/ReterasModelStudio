package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.WarcraftData;
import com.hiveworkshop.rms.parsers.w3o.Change;
import com.hiveworkshop.rms.parsers.w3o.ObjectDataChangeEntry;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorDataChangeListener;
import com.hiveworkshop.rms.util.War3ID;
import de.wc3data.stream.BlizzardDataInputStream;

import java.io.IOException;
import java.util.*;

public class MutableObjectData {
	protected final WorldEditorDataType worldEditorDataType;
	protected final WarcraftData sourceSLKData;
	protected final DataTable sourceSLKMetaData;
	protected final War3ObjectDataChangeset editorData;
	protected Set<War3ID> cachedKeySet;
	protected final Map<String, War3ID> metaNameToMetaId;
	protected final Map<War3ID, MutableGameObject> cachedKeyToGameObject;
	protected final MutableObjectDataChangeNotifier changeNotifier;

	public MutableObjectData(WorldEditorDataType worldEditorDataType, WarcraftData sourceSLKData,
	                         DataTable sourceSLKMetaData, War3ObjectDataChangeset editorData) {
		this.worldEditorDataType = worldEditorDataType;
		resolveStringReferencesInNames(sourceSLKData);
		this.sourceSLKData = sourceSLKData;
		this.sourceSLKMetaData = sourceSLKMetaData;
		this.editorData = editorData;
		metaNameToMetaId = new HashMap<>();
		for (String metaKeyString : sourceSLKMetaData.keySet()) {
			War3ID metaKey = War3ID.fromString(metaKeyString);
			metaNameToMetaId.put(sourceSLKMetaData.get(metaKeyString).getField("field"), metaKey);
		}
		cachedKeyToGameObject = new HashMap<>();
		changeNotifier = new MutableObjectDataChangeNotifier();
	}

	// TODO remove this hack
	public War3ObjectDataChangeset getEditorData() {
		return editorData;
	}

	public Map<String, War3ID> getMetaNameToMetaId() {
		return metaNameToMetaId;
	}

	private void resolveStringReferencesInNames(ObjectData sourceSLKData) {
		for (String key : sourceSLKData.keySet()) {
			GameObject gameObject = sourceSLKData.get(key);
			String suffix = gameObject.getField("EditorSuffix");
			String nameField = gameObject.getField("Name");
			if (nameField.startsWith("WESTRING")) {
				String name = getName2(nameField);
				gameObject.setField("Name", name);
			}
			if (suffix.startsWith("WESTRING")) {
				gameObject.setField("EditorSuffix", WEString.getString(suffix));
			}
		}
	}

	private String getName2(String nameField) {
		String name;
		if (!nameField.contains(" ")) {
			name = WEString.getString(nameField);
		} else {
			String[] names = nameField.split(" ");
			StringBuilder nameBuilder = new StringBuilder();
			for (String subName : names) {
				if (0 < nameBuilder.length()) {
					nameBuilder.append(" ");
				}
				if (subName.startsWith("WESTRING")) {
					nameBuilder.append(WEString.getString(subName));
				} else {
					nameBuilder.append(subName);
				}
			}
			name = nameBuilder.toString();
		}
		if (name.startsWith("\"") && name.endsWith("\"")) {
			return name.substring(1, name.length() - 1);
		}
		return name;
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
				if (0 < sb.length()) {
					sb.append(",");
				}
				sb.append(tech);
			}
			unit.setField(field, 0, sb.toString());
		}
	}

	private List<String> getTechList(Map<War3ID, War3ID> previousAliasToNewAlias, String techtreeString) {
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
		War3ID[] fieldsToCheck = getFieldsToCheck();
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
		MutableObjectData changeEditManager = new MutableObjectData(worldEditorDataType, sourceSLKData, sourceSLKMetaData, changeset);

		for (War3ID unitId : changeEditManager.keySet()) {
			MutableGameObject unit = changeEditManager.get(unitId);
			fixUnitTechTreeStrings(previousAliasToNewAlias, fieldsToCheck, unit);
		}
		return changeset;

	}

	protected War3ID[] getFieldsToCheck() {
		return worldEditorDataType == WorldEditorDataType.UNITS
				? new War3ID[] {War3ID.fromString("utra"), War3ID.fromString("uupt"), War3ID.fromString("ubui")}
				: new War3ID[] {};
	}

	public WorldEditorDataType getWorldEditorDataType() {
		return worldEditorDataType;
	}

	public DataTable getSourceSLKMetaData() {
		return sourceSLKMetaData;
	}

	public void addChangeListener(final UnitEditorDataChangeListener listener) {
		changeNotifier.subscribe(listener);
	}

	public void removeChangeListener(final UnitEditorDataChangeListener listener) {
		changeNotifier.unsubscribe(listener);
	}

	/**
	 * Returns the set of all Unit IDs in the map, at the cost of a lot of time to
	 * go find them all.
	 */

	public Set<War3ID> keySet() {
		if (cachedKeySet == null) {
			final Set<War3ID> customUnitKeys = editorData.getCustom().keySet();
			final Set<War3ID> customKeys = new HashSet<>(customUnitKeys);
			for (final String standardUnitKey : sourceSLKData.keySet()) {
				customKeys.add(War3ID.fromString(standardUnitKey));
			}
			cachedKeySet = customKeys;
		}
		return cachedKeySet;
	}

	public void dropCachesHack() {
		cachedKeySet = null;
		cachedKeyToGameObject.clear();
	}

	public MutableGameObject get(final War3ID id) {
		MutableGameObject mutableGameObject = cachedKeyToGameObject.get(id);
		if (mutableGameObject == null) {
			ObjectDataChangeEntry customUnitData = editorData.getFromID(id);
			if (customUnitData != null) {
				GameObject parentWC3Object = sourceSLKData.get(customUnitData.getOldId().asStringValue());
				mutableGameObject = new MutableGameObject(this, parentWC3Object, customUnitData, changeNotifier);
			} else if (sourceSLKData.get(id.asStringValue()) != null){
				mutableGameObject = new MutableGameObject(this, sourceSLKData.get(id.asStringValue()), null, changeNotifier);
				cachedKeyToGameObject.put(id, mutableGameObject);
			}
		}
		return mutableGameObject;
	}

	public MutableGameObject createNew(War3ID id, War3ID parent) {
		return createNew(id, parent, true);
	}

	protected MutableGameObject createNew(War3ID id, War3ID parent, boolean fireListeners) {
		editorData.getCustom().put(id, new ObjectDataChangeEntry(parent, id));
		if (cachedKeySet != null) {
			cachedKeySet.add(id);
		}
		if (fireListeners) {
			changeNotifier.objectCreated(id);
		}
		return get(id);
	}

	public void remove(final War3ID id) {
		remove(id, true);
	}

	public void remove(final List<MutableGameObject> objects) {
		List<War3ID> removedIds = new ArrayList<>();
		for (MutableGameObject object : objects) {
			if (object.isCustom()) {
				remove(object.getAlias(), false);
				removedIds.add(object.getAlias());
			}
		}
		changeNotifier.objectsRemoved(removedIds.toArray(new War3ID[0]));
	}

	private MutableGameObject remove(final War3ID id, final boolean fireListeners) {
		ObjectDataChangeEntry removedObject = editorData.getCustom().remove(id);
		MutableGameObject removedMutableObj = cachedKeyToGameObject.remove(id);
		if (cachedKeySet != null) {
			cachedKeySet.remove(id);
		}
		if (fireListeners) {
			changeNotifier.objectRemoved(id);
		}
		return removedMutableObj /* might be null based on cache, don't use */;
	}

	private static boolean goodForId(final char c) {
		return Character.isDigit(c) || ((c >= 'A') && (c <= 'Z'));
	}

	public War3ID getNextDefaultEditorId(final War3ID startingId) {
		War3ID newId = startingId;
		while (editorData.getCustom().containsKeyCaseInsensitive(newId)
				|| sourceSLKData.get(newId.toString()) != null
				|| !goodForId(newId.charAt(1))
				|| !goodForId(newId.charAt(2))
				|| !goodForId(newId.charAt(3))) {
			// TODO good code general solution
			if (newId.charAt(3) == 'Z') {
				if (newId.charAt(2) == 'Z') {
					if (newId.charAt(1) == 'Z') {
						newId = new War3ID(((newId.getValue() / (256 * 256 * 256)) * 256 * 256 * 256)
								+ (256 * 256 * 256) + '0' + ('0' * 256) + ('0' * 256 * 256));
					} else {
						newId = new War3ID(
								((newId.getValue() / (256 * 256)) * 256 * 256) + (256 * 256) + '0' + ('0' * 256));
					}
				} else {
					newId = new War3ID(((newId.getValue() / 256) * 256) + 256 + '0');
				}
			} else {
				newId = new War3ID(newId.getValue() + 1);
			}
		}
		return newId;
	}

	public static War3ID getNextDefaultEditorId(War3ID startingId, War3ObjectDataChangeset editorData, ObjectData sourceSLKData) {
		War3ID newId = startingId;
		while (editorData.getCustom().containsKeyCaseInsensitive(newId)
				|| sourceSLKData.get(newId.toString()) != null
				|| !goodForId(newId.charAt(1))
				|| !goodForId(newId.charAt(2))
				|| !goodForId(newId.charAt(3))) {
			newId = new War3ID(newId.getValue() + 1);
		}
		return newId;
	}


//	public static String getEditorMetaDataDisplayKey(int level, final GameObject metaData) {
//		int index = metaData.getFieldValue("index");
//		String metaDataName = metaData.getField("field");
//		int repeatCount = metaData.getFieldValue("repeat");
//		String upgradeHack = metaData.getField("appendIndex");
//		boolean repeats = (repeatCount > 0) && !"0".equals(upgradeHack);
//		int data = metaData.getFieldValue("data");
//		if (data > 0) {
//			metaDataName += (char) ('A' + (data - 1));
//		}
//		if ("1".equals(upgradeHack)) {
//			int upgradeExtensionLevel = level - 1;
//			if (upgradeExtensionLevel > 0) {
//				metaDataName += Integer.toString(upgradeExtensionLevel);
//			}
//		} else if (repeats && (index == -1)) {
//			if (level == 0) {
//				level = 1;
//			}
//			if (repeatCount >= 10) {
//				metaDataName += String.format("%2d", level).replace(' ', '0');
//			} else {
//				metaDataName += Integer.toString(level);
//			}
//		}
//		return metaDataName;
//	}

	public static String getDisplayAsRawDataName(final MutableGameObject gameObject) {
		String aliasString = gameObject.getAlias().toString();
		if (!gameObject.getAlias().equals(gameObject.getCode())) {
			aliasString += ":" + gameObject.getCode().toString();
		}
		return aliasString + " (" + gameObject.getName() + ")";
	}

	protected static War3ObjectDataChangeset getWar3ObjectDataChangeset(char expectedkind, String fileName) {
		War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset(expectedkind);
		try {
			CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();

			if (gameDataFileSystem.has(fileName)) {
				BlizzardDataInputStream stream = new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream(fileName));
				WTSFile wts = gameDataFileSystem.has("war3map.wts") ? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null;
				unitDataChangeset.load(stream, wts, true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return unitDataChangeset;
	}
}
