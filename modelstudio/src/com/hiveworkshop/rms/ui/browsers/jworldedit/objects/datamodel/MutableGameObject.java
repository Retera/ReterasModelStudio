package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.w3o.Change;
import com.hiveworkshop.rms.parsers.w3o.ChangeMap;
import com.hiveworkshop.rms.parsers.w3o.ObjectDataChangeEntry;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.War3ID;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MutableGameObject {
	private static final War3ID ROC_SUPPORT_URAC = War3ID.fromString("urac");
	private static final War3ID ROC_SUPPORT_UCAM = War3ID.fromString("ucam");
	private static final War3ID ROC_SUPPORT_USPE = War3ID.fromString("uspe");
	private static final War3ID ROC_SUPPORT_UBDG = War3ID.fromString("ubdg");

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
		// field structure fields - doesn't seem to be changeFlags 's' like you might
		// hope
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("ubdg")); // unit is a builder
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("dvar")); // doodad variations
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("alev")); // ability level
		FIELD_SETTINGS_FIELDS.add(War3ID.fromString("glvl")); // upgrade max level
	}


	private final MutableObjectDataChangeNotifier changeNotifier;
	private final MutableObjectData mutableObjectData;
	private final GameObject parentWC3Object;
	private ObjectDataChangeEntry customUnitData;

	public MutableGameObject(MutableObjectData mutableObjectData, GameObject parentWC3Object,
	                         ObjectDataChangeEntry customUnitData, MutableObjectDataChangeNotifier changeNotifier) {
		this.mutableObjectData = mutableObjectData;
		this.parentWC3Object = parentWC3Object;
		if (parentWC3Object == null) {
			System.err.println(
					"Parent object is null for " + customUnitData.getNewId() + ":" + customUnitData.getOldId());
			throw new AssertionError("parentWC3Object cannot be null");
//				this.parentWC3Object = new Element("", new DataTable());
		}
		this.customUnitData = customUnitData;
		this.changeNotifier = changeNotifier;
	}

	private void fireChangedEvent(final War3ID field, final int level) {
		final String changeFlags = mutableObjectData.getSourceSLKMetaData().get(field.toString()).getField("changeFlags");
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

	public ObjectDataChangeEntry getCustomUnitData() {
		return customUnitData;
	}

	public boolean hasCustomField(final War3ID field, final int level) {
		return getMatchingChange(field, level) != null;
	}

	public boolean hasEditorData() {
		return (customUnitData != null) && (customUnitData.getChanges().size() > 0);
	}

	public boolean isCustom() {
		return mutableObjectData.getEditorData().getCustom().containsKey(getAlias());
	}

	public void setField(final War3ID field, final int level, final String value) {
		if (value.equals(getFieldStringFromSLKs(field, level))) {
			if (!value.equals(getFieldAsString(field, level))) {
				fireChangedEvent(field, level);
				System.out.println("field was reset");
			} else {
				System.out.println("field was unmodified");
			}
			resetFieldToDefaults(field, level);
			return;
		}
		getOrCreateMatchingChange(field, level)
				.setStrval(value)
				.setVarTypeInt(War3ObjectDataChangeset.VAR_TYPE_STRING);
		System.out.println("field created change");
		fireChangedEvent(field, level);
	}

	public void setField(final War3ID field, final int level, final boolean value) {
		if (value == (asInt(getFieldStringFromSLKs(field, level).trim()) == 1)) {
			if (value != getFieldAsBoolean(field, level)) {
				fireChangedEvent(field, level);
			}
			resetFieldToDefaults(field, level);
			return;
		}
		getOrCreateMatchingChange(field, level)
				.setBoolval(value)
				.setVarTypeInt(War3ObjectDataChangeset.VAR_TYPE_BOOLEAN);
		fireChangedEvent(field, level);
	}

	public void setField(final War3ID field, final int level, final int value) {
		if (value == asInt(getFieldStringFromSLKs(field, level).trim())) {
			if (value != getFieldAsInteger(field, level)) {
				fireChangedEvent(field, level);
			}
			resetFieldToDefaults(field, level);
			return;
		}
		getOrCreateMatchingChange(field, level)
				.setLongval(value)
				.setVarTypeInt(War3ObjectDataChangeset.VAR_TYPE_INT);
		fireChangedEvent(field, level);
	}

	public void resetFieldToDefaults(final War3ID field, final int level) {
		final Change existingChange = getMatchingChange(field, level);
		if ((existingChange != null) && (customUnitData != null)) {
			customUnitData.getChanges().delete(field, existingChange);
			fireChangedEvent(field, level);
		}
	}

	public void setField(final War3ID field, final int level, final float value) {
		if (Math.abs(value - asFloat(getFieldStringFromSLKs(field, level).trim())) < 0.00001f) {
			if (Math.abs(value - getFieldAsFloat(field, level)) > 0.00001f) {
				fireChangedEvent(field, level);
			}
			resetFieldToDefaults(field, level);
			return;
		}
		final Change matchingChange = getOrCreateMatchingChange(field, level).setRealval(value);
		final boolean unsigned = mutableObjectData.getSourceSLKMetaData().get(field.asStringValue()).getField("type").equals("unreal");
		matchingChange.setVarTypeInt(
				unsigned ? War3ObjectDataChangeset.VAR_TYPE_UNREAL : War3ObjectDataChangeset.VAR_TYPE_REAL);
		fireChangedEvent(field, level);
	}

	private Change getOrCreateMatchingChange(final War3ID field, final int level) {
		if (customUnitData == null) {
			final War3ID war3Id = War3ID.fromString(parentWC3Object.getId());
			final ObjectDataChangeEntry newCustomUnitData = new ObjectDataChangeEntry(war3Id, War3ID.NONE);
			mutableObjectData.getEditorData().getOriginal().put(war3Id, newCustomUnitData);
			customUnitData = newCustomUnitData;
		}
		Change matchingChange = getMatchingChange(field, level);
		if (matchingChange == null) {
			final ChangeMap changeMap = customUnitData.getChanges();
			final List<Change> changeList = changeMap.get(field);
			matchingChange = new Change().setId(field).setLevel(level);
			if (mutableObjectData.getEditorData().extended()) {
				// dunno why, but Blizzard sure likes those dataptrs in the ability data
				// my code should grab 0 when the metadata lacks this field
				matchingChange.setDataptr(mutableObjectData.getSourceSLKMetaData().get(field.asStringValue()).getFieldValue("data"));
			}
			if (changeList == null) {
				changeMap.add(field, matchingChange);
			} else {
				boolean insertedChange = false;
				for (int i = 0; i < changeList.size(); i++) {
					if (changeList.get(i).getLevel() > level) {
						insertedChange = true;
						changeList.add(i, matchingChange);
						break;
					}
				}
				if (!insertedChange) {
					changeList.add(changeList.size(), matchingChange);
				}
			}
		}
		return matchingChange;
	}

	public String getFieldAsString(final War3ID field, final int level) {
		final Change matchingChange = getMatchingChange(field, level);
		if (matchingChange != null) {
			if (matchingChange.getVarTypeInt() != War3ObjectDataChangeset.VAR_TYPE_STRING) {
				throw new IllegalStateException(
						"Requested string value of '" + field + "' from '" + parentWC3Object.getId()
								+ "', but this field was not a string! vartype=" + matchingChange.getVarTypeInt());
			}
			return matchingChange.getStrval();
		}
		// no luck with custom data, look at the standard data
		int slkLevel = level;
		if (mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.UPGRADES) {
			slkLevel -= 1;
		}
		return getFieldStringFromSLKs(field, slkLevel);
	}

	private Change getMatchingChange(final War3ID field, final int level) {
		if (customUnitData == null) {
			return null;
		}
		List<Change> changeList = customUnitData.getChanges().get(field);
		if (changeList != null) {
			for (Change change : changeList) {
				if (change.getLevel() == level) {
					return change;
				}
			}
		}
		return null;
	}

	public String readSLKTag(final String key) {
		if (mutableObjectData.getMetaNameToMetaId().containsKey(key)) {
			return getFieldAsString(mutableObjectData.getMetaNameToMetaId().get(key), 0);
		}
		return parentWC3Object.getField(key);
	}

	public boolean readSLKTagBoolean(final String key) {
		if (mutableObjectData.getMetaNameToMetaId().containsKey(key)) {
			return getFieldAsBoolean(mutableObjectData.getMetaNameToMetaId().get(key), 0);
		}
		return parentWC3Object.getFieldValue(key) == 1;
	}

	public String getName1() {
		StringBuilder name = new StringBuilder(getFieldAsString(mutableObjectData.getEditorData().getNameField(),
				mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.UPGRADES ? 1 : 0));
		boolean nameKnown = name.length() >= 1;
		if (!nameKnown && !readSLKTag("code").equals(getAlias().toString()) && (readSLKTag("code").length() >= 4)
				&& !isCustom()) {
			final MutableGameObject codeObject = mutableObjectData.get(War3ID.fromString(readSLKTag("code").substring(0, 4)));
			if (codeObject != null) {
				name = new StringBuilder(codeObject.getName());
				nameKnown = true;
			}
		}

		String suf = switch (mutableObjectData.getWorldEditorDataType()) {
			case ABILITIES -> getFieldAsString(ABIL_EDITOR_SUFFIX, 0);
			case BUFFS_EFFECTS -> {
				final String editorName = getFieldAsString(BUFF_EDITOR_NAME, 0);
				if (!nameKnown && (editorName.length() > 1)) {
					name = new StringBuilder(editorName);
					nameKnown = true;
				}
				final String buffTip = getFieldAsString(BUFF_BUFFTIP, 0);
				if (!nameKnown && (buffTip.length() > 1)) {
					name = new StringBuilder(buffTip);
					nameKnown = true;
				}
				yield getFieldAsString(BUFF_EDITOR_SUFFIX, 0);
			}
			case DESTRUCTIBLES -> getFieldAsString(DESTRUCTABLE_EDITOR_SUFFIX, 0);
			case DOODADS, ITEM -> "";
			case UNITS -> {
				if (getFieldAsBoolean(UNIT_CAMPAIGN, 0) && Character.isUpperCase(getAlias().charAt(0))) {
					name = new StringBuilder(getFieldAsString(HERO_PROPER_NAMES, 0));
					if (name.toString().contains(",")) {
						name = new StringBuilder(name.toString().split(",")[0]);
					}
				}
				yield getFieldAsString(UNIT_EDITOR_SUFFIX, 0);
			}
			case UPGRADES -> getFieldAsString(UPGRADE_EDITOR_SUFFIX, 1);
		};

		if (nameKnown) {
			if (!name.toString().contains(" ")) {
				// name = WEString.getString(name);
			} else {
				final String[] names = name.toString().split(" ");
				name = new StringBuilder();
				for (final String subName : names) {
					if (name.length() > 0) {
						name.append(" ");
					}
					// if (subName.startsWith("WESTRING")) {
					// name += WEString.getString(subName);
					// } else {
					name.append(subName);
					// }
				}
			}
			if (name.toString().startsWith("\"") && name.toString().endsWith("\"")) {
				name = new StringBuilder(name.substring(1, name.length() - 1));
			}
		}
		if (!nameKnown) {
			name = new StringBuilder(WEString.getString("WESTRING_UNKNOWN") + " '" + getAlias().toString() + "'");
		}
		if ((suf.length() > 0) && !suf.equals("_")) {
			if (!suf.startsWith(" ")) {
				name.append(" ");
			}
			name.append(suf);
		}
		return name.toString();
	}

	public String getName() {
		WorldEditorDataType worldEditorDataType = mutableObjectData.getWorldEditorDataType();
		int level = worldEditorDataType == WorldEditorDataType.UPGRADES ? 1 : 0;
		String name = getFieldAsString(mutableObjectData.getEditorData().getNameField(), level);

		boolean nameKnown = name.length() >= 1;
		if (!nameKnown
				&& !readSLKTag("code").equals(getAlias().toString())
				&& (readSLKTag("code").length() >= 4)
				&& !isCustom()) {
			MutableGameObject codeObject = mutableObjectData.get(War3ID.fromString(readSLKTag("code").substring(0, 4)));
			if (codeObject != null) {
				name = codeObject.getName();
				nameKnown = true;
			}
		}

		String suf = switch (worldEditorDataType) {
			case ABILITIES -> getFieldAsString(ABIL_EDITOR_SUFFIX, 0);
			case BUFFS_EFFECTS -> getFieldAsString(BUFF_EDITOR_SUFFIX, 0);
			case DESTRUCTIBLES -> getFieldAsString(DESTRUCTABLE_EDITOR_SUFFIX, 0);
			case DOODADS, ITEM -> "";
			case UNITS -> getFieldAsString(UNIT_EDITOR_SUFFIX, 0);
			case UPGRADES -> getFieldAsString(UPGRADE_EDITOR_SUFFIX, 1);
		};

		String nameThing = getNameThingi(nameKnown, worldEditorDataType);
		if (!nameThing.equals("")) {
			if (worldEditorDataType == WorldEditorDataType.UNITS) {
				name = nameThing; // is this supposed to set nameKonwn = true?
			} else if (!nameKnown) {
				name = nameThing;
				nameKnown = true;
			}
		}


		if (nameKnown && !name.equals("")) {
			if (name.contains(" ")) {
				String[] names = name.split(" ");
				StringBuilder nameBuilder1 = new StringBuilder();
				for (final String subName : names) {
					if (nameBuilder1.length() > 0) {
						nameBuilder1.append(" ");
					}
					nameBuilder1.append(subName);
				}
				name = nameBuilder1.toString();
			}
			if (name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1);
//				nameBuilder = new StringBuilder(name);
			}
		} else if (!nameKnown) {
			name = WEString.getString("WESTRING_UNKNOWN") + " '" + getAlias().toString() + "'";
		}
		if (suf.length() > 0 && !suf.equals("_")) {
			if (!suf.startsWith(" ")) {
				name += " ";
			}
			name += suf;
		}
		return name;
	}

	private String getNameThingi(boolean nameKnown, WorldEditorDataType worldEditorDataType) {
		return switch (worldEditorDataType) {
			case ABILITIES, UPGRADES, DOODADS, ITEM, DESTRUCTIBLES -> "";
			case BUFFS_EFFECTS -> {
				if (!nameKnown) {
					String editorName = getFieldAsString(BUFF_EDITOR_NAME, 0);
					String buffTip = getFieldAsString(BUFF_BUFFTIP, 0);
					if (editorName.length() > 1) {
						yield editorName;
					} else if (buffTip.length() > 1) {
						yield buffTip;
					}
				}
				yield "";
			}
			case UNITS -> {
				if (getFieldAsBoolean(UNIT_CAMPAIGN, 0) && Character.isUpperCase(getAlias().charAt(0))) {
					String fieldAsString1 = getFieldAsString(HERO_PROPER_NAMES, 0);
					yield fieldAsString1.split(",")[0];
				}
				yield "";
			}
		};
	}

	private String getFieldStringFromSLKs(final War3ID field, final int level) {
		final GameObject metaData = mutableObjectData.getSourceSLKMetaData().get(field.asStringValue());
		if (metaData == null) {
			if (mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.UNITS) {
				if (ROC_SUPPORT_URAC.equals(field)) {
					return parentWC3Object.getField("race");
				} else if (ROC_SUPPORT_UCAM.equals(field)) {
					return "0";
				} else if (ROC_SUPPORT_USPE.equals(field)) {
					return parentWC3Object.getField("special");
				} else if (ROC_SUPPORT_UBDG.equals(field)) {
					return parentWC3Object.getField("isbldg");
				}
			}
			throw new IllegalStateException(
					"Program requested " + field.toString() + " from " + mutableObjectData.getWorldEditorDataType());
		}
		if (parentWC3Object == null) {
			throw new IllegalStateException("corrupted unit, no parent unit id");
		}
		int index = metaData.getFieldValue("index");
		final String upgradeHack = metaData.getField("appendIndex");
		if ("0".equals(upgradeHack)) {
			// Engage magic upgrade hack to replace index with level
			if (!field.toString().equals("gbpx") && !field.toString().equals("gbpy")) {
				index = level;
			}
		} else if ((index != -1) && (level > 0)) {
			index = level - 1;
		}
		String editorMetaDataDisplayKey = MutableObjectData.getEditorMetaDataDisplayKey(level, metaData);
		if (SaveProfile.get().isHd() && parentWC3Object.keySet().contains(editorMetaDataDisplayKey + ":hd")) {
			editorMetaDataDisplayKey = editorMetaDataDisplayKey + ":hd";
		}
		if (index != -1) {
			return parentWC3Object.getField(editorMetaDataDisplayKey, index);
		}
		return parentWC3Object.getField(editorMetaDataDisplayKey);
	}

	public int getFieldAsInteger(final War3ID field, final int level) {
		final Change matchingChange = getMatchingChange(field, level);
		if (matchingChange != null) {
			if (matchingChange.getVarTypeInt() != War3ObjectDataChangeset.VAR_TYPE_INT) {
				throw new IllegalStateException(
						"Requested integer value of '" + field + "' from '" + parentWC3Object.getId()
								+ "', but this field was not an int! vartype=" + matchingChange.getVarTypeInt());
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
			if (matchingChange.getVarTypeInt() != War3ObjectDataChangeset.VAR_TYPE_BOOLEAN) {
				if (matchingChange.getVarTypeInt() == War3ObjectDataChangeset.VAR_TYPE_INT) {
					return matchingChange.getLongval() == 1;
				} else {
					throw new IllegalStateException(
							"Requested boolean value of '" + field + "' from '" + parentWC3Object.getId()
									+ "', but this field was not a bool! vartype=" + matchingChange.getVarTypeInt());
				}
			}
			return matchingChange.getBoolval();
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
			if ((matchingChange.getVarTypeInt() != War3ObjectDataChangeset.VAR_TYPE_REAL)
					&& (matchingChange.getVarTypeInt() != War3ObjectDataChangeset.VAR_TYPE_UNREAL)) {
				throw new IllegalStateException(
						"Requested float value of '" + field + "' from '" + parentWC3Object.getId()
								+ "', but this field was not a float! vartype=" + matchingChange.getVarTypeInt());
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
			if ((mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.ABILITIES
					|| mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.BUFFS_EFFECTS)
					&& (parentWC3Object.getField("code") != null
					&& parentWC3Object.getField("code").length() > 0)) {
				return War3ID.fromString(parentWC3Object.getField("code"));
			} else {
				return War3ID.fromString(parentWC3Object.getId());
			}
		}
		if (War3ID.NONE.equals(customUnitData.getNewId())) {
			if (mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.ABILITIES
					|| mutableObjectData.getWorldEditorDataType() == WorldEditorDataType.BUFFS_EFFECTS) {
				return War3ID.fromString(parentWC3Object.getField("code"));
			} else {
				return customUnitData.getOldId();
			}
		}
		return customUnitData.getOldId();
	}


	private static int asInt(final String text) {
		return text == null ? 0
				: "".equals(text) ? 0 : "-".equals(text) ? 0 : "_".equals(text) ? 0 : Integer.parseInt(text);
	}

	private static float asFloat(final String text) {
		return text == null ? 0
				: "".equals(text) ? 0 : "-".equals(text) ? 0 : "_".equals(text) ? 0 : Float.parseFloat(text);
	}

}
