package com.hiveworkshop.wc3.units.objectdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.etheller.collections.ArrayList;
import com.etheller.collections.List;
import com.etheller.collections.MapView;
import com.etheller.collections.MapView.Entry;
import com.etheller.collections.SetView;
import com.etheller.util.CollectionUtils;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.sun.istack.internal.Nullable;

public final class MutableObjectData {
	private final WorldEditorDataType worldEditorDataType;
	private final ObjectData sourceSLKData;
	private final ObjectData sourceSLKMetaData;
	private final War3ObjectDataChangeset editorData;
	private Set<War3ID> cachedKeySet;
	private final Map<String, War3ID> metaNameToMetaId;
	private final Map<War3ID, MutableGameObject> cachedKeyToGameObject;
	private final MutableObjectDataChangeNotifier changeNotifier;

	public MutableObjectData(final WorldEditorDataType worldEditorDataType, final ObjectData sourceSLKData,
			final ObjectData sourceSLKMetaData, final War3ObjectDataChangeset editorData) {
		this.worldEditorDataType = worldEditorDataType;
		resolveStringReferencesInNames(sourceSLKData);
		this.sourceSLKData = sourceSLKData;
		this.sourceSLKMetaData = sourceSLKMetaData;
		this.editorData = editorData;
		this.metaNameToMetaId = new HashMap<>();
		for (final String metaKeyString : sourceSLKMetaData.keySet()) {
			final War3ID metaKey = War3ID.fromString(metaKeyString);
			metaNameToMetaId.put(sourceSLKMetaData.get(metaKeyString).getField("field"), metaKey);
		}
		this.cachedKeyToGameObject = new HashMap<>();
		this.changeNotifier = new MutableObjectDataChangeNotifier();
	}

	private void resolveStringReferencesInNames(final ObjectData sourceSLKData) {
		for (final String key : sourceSLKData.keySet()) {
			final GameObject gameObject = sourceSLKData.get(key);
			String name = gameObject.getField("Name");
			final String suffix = gameObject.getField("EditorSuffix");
			if (name.startsWith("WESTRING")) {
				if (!name.contains(" ")) {
					name = WEString.getString(name);
				} else {
					final String[] names = name.split(" ");
					name = "";
					for (final String subName : names) {
						if (name.length() > 0) {
							name += " ";
						}
						if (subName.startsWith("WESTRING")) {
							name += WEString.getString(subName);
						} else {
							name += subName;
						}
					}
				}
				if (name.startsWith("\"") && name.endsWith("\"")) {
					name = name.substring(1, name.length() - 1);
				}
				gameObject.setField("Name", name);
			}
			if (suffix.startsWith("WESTRING")) {
				gameObject.setField("EditorSuffix", WEString.getString(suffix));
			}
		}
	}

	public void mergeChangset(final War3ObjectDataChangeset changeset) {
		final List<War3ID> newObjects = new ArrayList<>();
		for (final MapView.Entry<War3ID, ObjectDataChangeEntry> entry : changeset.getCustom()) {
			final War3ID nextDefaultEditorId = getNextDefaultEditorId(
					War3ID.fromString(entry.getKey().charAt(0) + "000"));
			// createNew API will notifier the changeNotifier
			final MutableGameObject newObject = createNew(nextDefaultEditorId, entry.getValue().getOldId(), false);
			for (final MapView.Entry<War3ID, List<Change>> changeList : entry.getValue().getChanges()) {
				newObject.customUnitData.getChanges().add(changeList.getKey(), changeList.getValue());
			}
			newObjects.add(nextDefaultEditorId);
		}
		changeNotifier.objectsCreated(newObjects.toArray(new War3ID[newObjects.size()]));
	}

	public War3ObjectDataChangeset copySelectedObjects(final List<MutableGameObject> objectsToCopy) {
		final War3ObjectDataChangeset changeset = new War3ObjectDataChangeset();
		for (final MutableGameObject gameObject : objectsToCopy) {
			final ObjectDataChangeEntry gameObjectUserDataToCopy;
			final ObjectDataChangeEntry gameObjectUserData;
			final War3ID alias = gameObject.getAlias();
			if (editorData.getOriginal().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getOriginal().get(alias);
				gameObjectUserData = new ObjectDataChangeEntry(gameObjectUserDataToCopy.getOldId(),
						gameObjectUserDataToCopy.getNewId());
			} else if (editorData.getCustom().containsKey(alias)) {
				gameObjectUserDataToCopy = editorData.getCustom().get(alias);
				gameObjectUserData = new ObjectDataChangeEntry(gameObjectUserDataToCopy.getOldId(),
						gameObjectUserDataToCopy.getNewId());
			} else {
				gameObjectUserDataToCopy = null;
				gameObjectUserData = new ObjectDataChangeEntry(
						gameObject.isCustom() ? gameObject.getCode() : gameObject.getCode(),
						changeset.getunusedid(gameObject.getCode()));
			}
			if (gameObjectUserDataToCopy != null) {
				for (final Entry<War3ID, List<Change>> changeEntry : gameObjectUserDataToCopy.getChanges()) {
					for (final Change change : changeEntry.getValue()) {
						final Change newChange = new Change();
						newChange.copyFrom(change);
						gameObjectUserData.getChanges().add(change.getId(), newChange);
					}
				}
			}
			changeset.getCustom().put(gameObject.getAlias(), gameObjectUserData);
		}
		return changeset;

	}

	public ObjectData getSourceSLKMetaData() {
		return sourceSLKMetaData;
	}

	public void addChangeListener(final MutableObjectDataChangeListener listener) {
		changeNotifier.subscribe(listener);
	}

	public void removeChangeListener(final MutableObjectDataChangeListener listener) {
		changeNotifier.unsubscribe(listener);
	}

	/**
	 * Returns the set of all Unit IDs in the map, at the cost of a lot of time to go find them all.
	 *
	 * @return
	 */
	public Set<War3ID> keySet() {
		if (cachedKeySet == null) {
			final SetView<War3ID> customUnitKeys = editorData.getCustom().keySet();
			final Set<War3ID> customKeys = new HashSet<>(CollectionUtils.toJava(customUnitKeys));
			for (final String standardUnitKey : sourceSLKData.keySet()) {
				customKeys.add(War3ID.fromString(standardUnitKey));
			}
			cachedKeySet = customKeys;
		}
		return cachedKeySet;
	}

	@Nullable()
	public MutableGameObject get(final War3ID id) {
		MutableGameObject mutableGameObject = cachedKeyToGameObject.get(id);
		if (mutableGameObject == null) {
			if (editorData.getCustom().containsKey(id)) {
				final ObjectDataChangeEntry customUnitData = editorData.getCustom().get(id);
				mutableGameObject = new MutableGameObject(sourceSLKData.get(customUnitData.getOldId().asStringValue()),
						customUnitData);
				cachedKeyToGameObject.put(id, mutableGameObject);
			} else if (editorData.getOriginal().containsKey(id)) {
				final ObjectDataChangeEntry customUnitData = editorData.getOriginal().get(id);
				mutableGameObject = new MutableGameObject(sourceSLKData.get(customUnitData.getOldId().asStringValue()),
						editorData.getOriginal().get(id));
				cachedKeyToGameObject.put(id, mutableGameObject);
			} else if (sourceSLKData.get(id.asStringValue()) != null) {
				mutableGameObject = new MutableGameObject(sourceSLKData.get(id.asStringValue()), null);
				cachedKeyToGameObject.put(id, mutableGameObject);
			}
		}
		return mutableGameObject;
	}

	public MutableGameObject createNew(final War3ID id, final War3ID parent) {
		return createNew(id, parent, true);
	}

	private MutableGameObject createNew(final War3ID id, final War3ID parent, final boolean fireListeners) {
		editorData.getCustom().put(id, new ObjectDataChangeEntry(parent, id));
		cachedKeySet.add(id);
		if (fireListeners) {
			changeNotifier.objectCreated(id);
		}
		return get(id);
	}

	private static boolean goodForId(final char c) {
		return Character.isDigit(c) || (c >= 'A' && c <= 'Z');
	}

	public War3ID getNextDefaultEditorId(final War3ID startingId) {
		War3ID newId = startingId;
		while (editorData.getCustom().containsKey(newId) || sourceSLKData.get(newId.toString()) != null
				|| !goodForId(newId.charAt(1)) || !goodForId(newId.charAt(2)) || !goodForId(newId.charAt(3))) {
			newId = new War3ID(newId.getValue() + 1);
		}
		return newId;
	}

	private static final War3ID BUFF_EDITOR_NAME = War3ID.fromString("fnam");
	private static final War3ID BUFF_BUFFTIP = War3ID.fromString("ftip");
	private static final War3ID UNIT_CAMPAIGN = War3ID.fromString("ucam");
	private static final War3ID UNIT_EDITOR_SUFFIX = War3ID.fromString("unsf");
	private static final War3ID ABIL_EDITOR_SUFFIX = War3ID.fromString("ansf");
	private static final War3ID DESTRUCTABLE_EDITOR_SUFFIX = War3ID.fromString("bsuf");
	private static final War3ID BUFF_EDITOR_SUFFIX = War3ID.fromString("fnsf");
	private static final War3ID UPGRADE_EDITOR_SUFFIX = War3ID.fromString("gnsf");
	private static final War3ID HERO_PROPER_NAMES = War3ID.fromString("upro");

	private static final Set<War3ID> CATEGORY_FIELDS = new HashSet<>();
	private static final Set<War3ID> TEXT_FIELDS = new HashSet<>();
	private static final Set<War3ID> ICON_FIELDS = new HashSet<>();
	private static final Set<War3ID> FIELD_SETTINGS_FIELDS = new HashSet<>();
	static {
		// categorizing - I thought these would be changeFlags value "c", but no luck
		CATEGORY_FIELDS.add(War3ID.fromString("ubdg")); // is a building
		CATEGORY_FIELDS.add(War3ID.fromString("uspe")); // categorize special
		CATEGORY_FIELDS.add(War3ID.fromString("ucam")); // categorize campaign
		CATEGORY_FIELDS.add(War3ID.fromString("urac")); // race
		CATEGORY_FIELDS.add(War3ID.fromString("uine")); // in editor
		CATEGORY_FIELDS.add(War3ID.fromString("ucls")); // sort string (not a real field, fanmade)

		CATEGORY_FIELDS.add(War3ID.fromString("icla")); // item class

		CATEGORY_FIELDS.add(War3ID.fromString("bcat")); // destructible category

		CATEGORY_FIELDS.add(War3ID.fromString("dcat")); // doodad category

		CATEGORY_FIELDS.add(War3ID.fromString("aher")); // hero ability
		CATEGORY_FIELDS.add(War3ID.fromString("aite")); // item ability
		CATEGORY_FIELDS.add(War3ID.fromString("arac")); // ability race

		CATEGORY_FIELDS.add(War3ID.fromString("frac")); // buff race
		CATEGORY_FIELDS.add(War3ID.fromString("feff")); // is effect

		CATEGORY_FIELDS.add(War3ID.fromString("grac")); // upgrade race
		// field structure fields - doesn't seem to be changeFlags 's' like you might hope
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("ubdg")); // unit is a builder
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("dvar")); // doodad variations
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("alev")); // ability level
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("glvl")); // upgrade max level
	}

	public final class MutableGameObject {
		private final GameObject parentWC3Object;
		private ObjectDataChangeEntry customUnitData;

		private void fireChangedEvent(final War3ID field, final int level) {
			final String changeFlags = sourceSLKMetaData.get(field.toString()).getField("changeFlags");
			if (CATEGORY_FIELDS.contains(field)) {
				changeNotifier.categoriesChanged(getAlias());
			} else if (changeFlags.contains("t")) {
				changeNotifier.textChanged(getAlias());
			} else if (changeFlags.contains("m")) {
				changeNotifier.modelChanged(getAlias());
			} else if (changeFlags.contains("i")) {
				changeNotifier.iconsChanged(getAlias());
			} else if (FIELD_SETTINGS_FIELDS.contains(field)) {
				changeNotifier.fieldsChanged(getAlias());
			}
		}

		public MutableGameObject(final GameObject parentWC3Object, final ObjectDataChangeEntry customUnitData) {
			this.parentWC3Object = parentWC3Object;
			if (parentWC3Object == null) {
				throw new AssertionError("parentWC3Object cannot be null");
			}
			this.customUnitData = customUnitData;
		}

		public boolean hasCustomField(final War3ID field, final int level) {
			return getMatchingChange(field, level) != null;
		}

		public boolean hasEditorData() {
			return customUnitData != null && customUnitData.getChanges().size() > 0;
		}

		public boolean isCustom() {
			return editorData.getCustom().containsKey(getAlias());
		}

		public void setField(final War3ID field, final int level, final String value) {
			if (value.equals(getFieldStringFromSLKs(field, level))) {
				if (!value.equals(getFieldAsString(field, level))) {
					fireChangedEvent(field, level);
				}
				resetFieldToDefaults(field, level);
				return;
			}
			final Change matchingChange = getOrCreateMatchingChange(field, level);
			matchingChange.setStrval(value);
			matchingChange.setVartype(War3ObjectDataChangeset.VAR_TYPE_STRING);
			fireChangedEvent(field, level);
		}

		public void setField(final War3ID field, final int level, final boolean value) {
			if (value == (Integer.parseInt(getFieldStringFromSLKs(field, level)) == 1)) {
				if (value != (getFieldAsBoolean(field, level))) {
					fireChangedEvent(field, level);
				}
				resetFieldToDefaults(field, level);
				return;
			}
			final Change matchingChange = getOrCreateMatchingChange(field, level);
			matchingChange.setBoolval(value);
			matchingChange.setVartype(War3ObjectDataChangeset.VAR_TYPE_BOOLEAN);
			fireChangedEvent(field, level);
		}

		public void setField(final War3ID field, final int level, final int value) {
			if (value == (Integer.parseInt(getFieldStringFromSLKs(field, level)))) {
				if (value != (getFieldAsInteger(field, level))) {
					fireChangedEvent(field, level);
				}
				resetFieldToDefaults(field, level);
				return;
			}
			final Change matchingChange = getOrCreateMatchingChange(field, level);
			matchingChange.setLongval(value);
			matchingChange.setVartype(War3ObjectDataChangeset.VAR_TYPE_INT);
			fireChangedEvent(field, level);
		}

		public void resetFieldToDefaults(final War3ID field, final int level) {
			final Change existingChange = getMatchingChange(field, level);
			if (existingChange != null && customUnitData != null) {
				customUnitData.getChanges().delete(field, existingChange);
				fireChangedEvent(field, level);
			}
			return;
		}

		public void setField(final War3ID field, final int level, final float value) {
			if (Math.abs(value - (Float.parseFloat(getFieldStringFromSLKs(field, level)))) < 0.00001f) {
				if (Math.abs(value - getFieldAsFloat(field, level)) > 0.00001f) {
					fireChangedEvent(field, level);
				}
				resetFieldToDefaults(field, level);
				return;
			}
			final Change matchingChange = getOrCreateMatchingChange(field, level);
			matchingChange.setRealval(value);
			final boolean unsigned = sourceSLKMetaData.get(field.asStringValue()).getField("type").equals("unreal");
			matchingChange.setVartype(
					unsigned ? War3ObjectDataChangeset.VAR_TYPE_UNREAL : War3ObjectDataChangeset.VAR_TYPE_REAL);
			fireChangedEvent(field, level);
		}

		private Change getOrCreateMatchingChange(final War3ID field, final int level) {
			if (customUnitData == null) {
				final War3ID war3Id = War3ID.fromString(parentWC3Object.getId());
				final ObjectDataChangeEntry newCustomUnitData = new ObjectDataChangeEntry(war3Id, War3ID.NONE);
				editorData.getOriginal().put(war3Id, newCustomUnitData);
				customUnitData = newCustomUnitData;
			}
			Change matchingChange = getMatchingChange(field, level);
			if (matchingChange == null) {
				final ChangeMap changeMap = customUnitData.getChanges();
				final List<Change> changeList = changeMap.get(field);
				matchingChange = new Change();
				matchingChange.setLevel(level);
				if (editorData.extended()) {
					// dunno why, but Blizzard sure likes those dataptrs in the ability data
					// my code should grab 0 when the metadata lacks this field
					matchingChange.setDataptr(sourceSLKMetaData.get(field.asStringValue()).getFieldValue("data"));
				}
				if (changeList == null) {
					changeMap.add(field, matchingChange);
				} else {
					for (int i = 0; i < changeList.size(); i++) {
						if (changeList.get(i).getLevel() > level) {
							changeList.add(i, matchingChange);
							break;
						}
					}
				}
			}
			return matchingChange;
		}

		public String getFieldAsString(final War3ID field, final int level) {
			final Change matchingChange = getMatchingChange(field, level);
			if (matchingChange != null) {
				if (matchingChange.getVartype() != War3ObjectDataChangeset.VAR_TYPE_STRING) {
					throw new IllegalStateException(
							"Requested string value of '" + field + "' from '" + parentWC3Object.getId()
									+ "', but this field was not a string! vartype=" + matchingChange.getVartype());
				}
				return matchingChange.getStrval();
			}
			// no luck with custom data, look at the standard data
			return getFieldStringFromSLKs(field, level);
		}

		private Change getMatchingChange(final War3ID field, final int level) {
			Change matchingChange = null;
			if (customUnitData == null) {
				return null;
			}
			final List<Change> changeList = customUnitData.getChanges().get(field);
			if (changeList != null) {
				for (final Change change : changeList) {
					if (change.getLevel() == level) {
						matchingChange = change;
						break;
					}
				}
			}
			return matchingChange;
		}

		public String readSLKTag(final String key) {
			if (metaNameToMetaId.containsKey(key)) {
				return getFieldAsString(metaNameToMetaId.get(key), 0);
			}
			return parentWC3Object.getField(key);
		}

		public boolean readSLKTagBoolean(final String key) {
			if (metaNameToMetaId.containsKey(key)) {
				return getFieldAsBoolean(metaNameToMetaId.get(key), 0);
			}
			return parentWC3Object.getFieldValue(key) == 1;
		}

		public String getName() {
			String name = getFieldAsString(editorData.getNameField(), 0);
			boolean nameKnown = name.length() >= 1;
			if (!nameKnown && !readSLKTag("code").equals(getAlias().toString()) && readSLKTag("code").length() >= 4
					&& !isCustom()) {
				final MutableGameObject codeObject = get(War3ID.fromString(readSLKTag("code").substring(0, 4)));
				if (codeObject != null) {
					name = codeObject.getName();
					nameKnown = true;
				}
			}
			String suf = "";
			switch (worldEditorDataType) {
			case ABILITIES:
				suf = getFieldAsString(ABIL_EDITOR_SUFFIX, 0);
				break;
			case BUFFS_EFFECTS:
				final String editorName = getFieldAsString(BUFF_EDITOR_NAME, 0);
				if (!nameKnown && editorName.length() > 1) {
					name = editorName;
					nameKnown = true;
				}
				final String buffTip = getFieldAsString(BUFF_BUFFTIP, 0);
				if (!nameKnown && buffTip.length() > 1) {
					name = buffTip;
					nameKnown = true;
				}
				suf = getFieldAsString(BUFF_EDITOR_SUFFIX, 0);
				break;
			case DESTRUCTIBLES:
				suf = getFieldAsString(DESTRUCTABLE_EDITOR_SUFFIX, 0);
				break;
			case DOODADS:
				break;
			case ITEM:
				break;
			case UNITS:
				if (getFieldAsBoolean(UNIT_CAMPAIGN, 0) && Character.isUpperCase(getAlias().charAt(0))) {
					name = getFieldAsString(HERO_PROPER_NAMES, 0);
					if (name.contains(",")) {
						name = name.split(",")[0];
					}
				}
				suf = getFieldAsString(UNIT_EDITOR_SUFFIX, 0);
				break;
			case UPGRADES:
				suf = getFieldAsString(UPGRADE_EDITOR_SUFFIX, 0);
				break;
			}
			if (nameKnown/* && name.startsWith("WESTRING") */) {
				if (!name.contains(" ")) {
					// name = WEString.getString(name);
				} else {
					final String[] names = name.split(" ");
					name = "";
					for (final String subName : names) {
						if (name.length() > 0) {
							name += " ";
						}
						// if (subName.startsWith("WESTRING")) {
						// name += WEString.getString(subName);
						// } else {
						name += subName;
						// }
					}
				}
				if (name.startsWith("\"") && name.endsWith("\"")) {
					name = name.substring(1, name.length() - 1);
				}
			}
			if (!nameKnown) {
				name = WEString.getString("WESTRING_UNKNOWN") + " '" + getAlias().toString() + "'";
			}
			if (suf.length() > 0 && !suf.equals("_")) {
				// if (suf.startsWith("WESTRING")) {
				// suf = WEString.getString(suf);
				// }
				if (!suf.startsWith(" ")) {
					name += " ";
				}
				name += suf;
			}
			return name;
		}

		private String getFieldStringFromSLKs(final War3ID field, final int level) {
			final GameObject metaData = sourceSLKMetaData.get(field.asStringValue());
			if (metaData == null) {
				throw new IllegalStateException(
						"Program requested " + field.toString() + " from " + worldEditorDataType);
			}
			if (parentWC3Object == null) {
				throw new IllegalStateException("corrupted unit, no parent unit id");
			}
			final String fieldStringValue = parentWC3Object.getField(getEditorMetaDataDisplayKey(level, metaData));
			int index = metaData.getFieldValue("index");
			final String upgradeHack = metaData.getField("appendIndex");
			if ("0".equals(upgradeHack)) {
				// Engage magic upgrade hack to replace index with level
				index = level;
			}
			if (index != -1) {
				final String[] split = fieldStringValue.split(",");
				if (index < split.length) {
					return split[index];
				}
			}
			return fieldStringValue;
		}

		public int getFieldAsInteger(final War3ID field, final int level) {
			final Change matchingChange = getMatchingChange(field, level);
			if (matchingChange != null) {
				if (matchingChange.getVartype() != War3ObjectDataChangeset.VAR_TYPE_INT) {
					throw new IllegalStateException(
							"Requested string value of '" + field + "' from '" + parentWC3Object.getId()
									+ "', but this field was not an int! vartype=" + matchingChange.getVartype());
				}
				return matchingChange.getLongval();
			}
			// no luck with custom data, look at the standard data
			try {
				return Integer.parseInt(getFieldStringFromSLKs(field, level));
			} catch (final NumberFormatException e) {
				return 0;
			}
		}

		public boolean getFieldAsBoolean(final War3ID field, final int level) {
			final Change matchingChange = getMatchingChange(field, level);
			if (matchingChange != null) {
				if (matchingChange.getVartype() != War3ObjectDataChangeset.VAR_TYPE_BOOLEAN) {
					throw new IllegalStateException(
							"Requested string value of '" + field + "' from '" + parentWC3Object.getId()
									+ "', but this field was not an int! vartype=" + matchingChange.getVartype());
				}
				return matchingChange.isBoolval();
			}
			// no luck with custom data, look at the standard data
			try {
				return Integer.parseInt(getFieldStringFromSLKs(field, level)) == 1;
			} catch (final NumberFormatException e) {
				return false;
			}
		}

		public float getFieldAsFloat(final War3ID field, final int level) {
			final Change matchingChange = getMatchingChange(field, level);
			if (matchingChange != null) {
				if (matchingChange.getVartype() != War3ObjectDataChangeset.VAR_TYPE_REAL
						&& matchingChange.getVartype() != War3ObjectDataChangeset.VAR_TYPE_UNREAL) {
					throw new IllegalStateException(
							"Requested string value of '" + field + "' from '" + parentWC3Object.getId()
									+ "', but this field was not a float! vartype=" + matchingChange.getVartype());
				}
				return matchingChange.getRealval();
			}
			// no luck with custom data, look at the standard data
			try {
				return Float.parseFloat(getFieldStringFromSLKs(field, level));
			} catch (final NumberFormatException e) {
				return 0;
			}
		}

		public War3ID getAlias() {
			if (customUnitData == null) {
				return War3ID.fromString(parentWC3Object.getId());
			}
			if (War3ID.NONE.equals(customUnitData.getNewId())) {
				return customUnitData.getOldId();
			}
			return customUnitData.getNewId();
		}

		public War3ID getCode() {
			if (customUnitData == null) {
				if (worldEditorDataType == WorldEditorDataType.ABILITIES
						|| worldEditorDataType == WorldEditorDataType.BUFFS_EFFECTS) {
					return War3ID.fromString(parentWC3Object.getField("code"));
				} else {
					return War3ID.fromString(parentWC3Object.getId());
				}
			}
			if (War3ID.NONE.equals(customUnitData.getNewId())) {
				if (worldEditorDataType == WorldEditorDataType.ABILITIES
						|| worldEditorDataType == WorldEditorDataType.BUFFS_EFFECTS) {
					return War3ID.fromString(parentWC3Object.getField("code"));
				} else {
					return customUnitData.getOldId();
				}
			}
			return customUnitData.getOldId();
		}

	}

	public enum WorldEditorDataType {
		UNITS, ITEM, DESTRUCTIBLES, DOODADS, ABILITIES, BUFFS_EFFECTS, UPGRADES;
	}

	public static String getEditorMetaDataDisplayKey(int level, final GameObject metaData) {
		String metaDataName = metaData.getField("field");
		final int repeatCount = metaData.getFieldValue("repeat");
		final String upgradeHack = metaData.getField("appendIndex");
		final boolean repeats = repeatCount > 0 && !("0".equals(upgradeHack));
		final int data = metaData.getFieldValue("data");
		if (data > 0) {
			metaDataName += (char) ('A' + (data - 1));
		}
		if ("1".equals(upgradeHack)) {
			final int upgradeExtensionLevel = level - 1;
			if (upgradeExtensionLevel > 0) {
				metaDataName += Integer.toString(upgradeExtensionLevel);
			}
		} else if (repeats) {
			if (level == 0) {
				level = 1;
			}
			if (repeatCount >= 10) {
				metaDataName += String.format("%2d", level).replace(' ', '0');
			} else {
				metaDataName += Integer.toString(level);
			}
		}
		return metaDataName;
	}
}
