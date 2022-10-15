package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableUpgradeData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WE_STRING;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.upgrades.UpgradeSortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.*;

public class UpgradeTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {
	protected final War3ID levelField;
	private final Map<String, GameObject> effectIDToUpgradeEffect = new HashMap<>();

	public UpgradeTabTreeBrowserBuilder(){
		super(WorldEditorDataType.UPGRADES);
		this.levelField = WE_Field.UPGRADE_MAX_LEVEL.getId();
		ObjectData upgradeEffectMetaData = StandardObjectData.getStandardUpgradeEffectMeta();
		for (String notEffectId : upgradeEffectMetaData.keySet()) {
			GameObject upgradeEffect = upgradeEffectMetaData.get(notEffectId);
			String key = upgradeEffect.getField("effectID") + upgradeEffect.getField("dataType");
			effectIDToUpgradeEffect.put(key, upgradeEffect);
		}
		unitData = new MutableUpgradeData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UPGR", "Upgr");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData(){
		unitData = new MutableUpgradeData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_STANDARDUPGRS")),
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_CUSTOMUPGRS")));
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField) {
		String effectType = metaDataField.getField("effectType");
		if ("Base".equalsIgnoreCase(effectType)
				|| "Mod".equalsIgnoreCase(effectType)
				|| "Code".equalsIgnoreCase(effectType)) {
			War3ID field = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
			String key = gameObject.getFieldAsString(field, 0) + metaDataField.getField("effectType");
			return effectIDToUpgradeEffect.containsKey(key);
		}
		return true;
	}

	@Override
	protected List<AbstractObjectField> makeFields(War3ID metaKey, GameObject metaField, MutableGameObject gameObject, ObjectData metaData) {
		int repeatCount = metaField.getFieldValue("repeat");
		int actualRepeatCount = gameObject.getFieldAsInteger(levelField, 0);
		if (1 <= repeatCount && 1 < actualRepeatCount) {
			List<AbstractObjectField> fields = new ArrayList<>();
			for (int level = 1; level <= actualRepeatCount; level++) {
				boolean hasMoreThanOneLevel = true;

				String displayName = getDisplayName(metaField, level, gameObject);
				String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

				String displayPrefix = getDisplayPrefix(level);
				String prefixedDispName = displayPrefix + displayName;

				AbstractObjectField field = getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
				fields.add(field);
			}
			return fields;
		} else {
			int level = 1 <= repeatCount ? 1 : 0;
			boolean hasMoreThanOneLevel = false;

			int displayLevel = 0;
			String displayName = getDisplayName(metaField, displayLevel, gameObject);
			String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

			String prefixedDispName = displayName;

			AbstractObjectField field = getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
			return Collections.singletonList(field);
		}
	}
	protected List<AbstractObjectField> makeFields1(War3ID metaKey, GameObject metaField, MutableGameObject gameObject, ObjectData metaData) {
		int repeatCount = metaField.getFieldValue("repeat");
		int actualRepeatCount = gameObject.getFieldAsInteger(levelField, 0);
		if (1 <= repeatCount && 1 < actualRepeatCount) {
			List<AbstractObjectField> fields = new ArrayList<>();
			for (int level = 1; level <= actualRepeatCount; level++) {
				fields.add(create(metaField, gameObject, metaKey, level, true));
			}
			return fields;
		} else {
			return Collections.singletonList(create(metaField, gameObject, metaKey, 1 <= repeatCount ? 1 : 0, false));
		}
	}

	@Override
	protected String getDisplayName(GameObject metaDataField, int level, MutableGameObject gameObject) {
		String defaultDisplayName = getDisplayName2(metaDataField);
		String effectType = metaDataField.getField("effectType");
		if ("Base".equalsIgnoreCase(effectType)
				|| "Mod".equalsIgnoreCase(effectType)
				|| "Code".equalsIgnoreCase(effectType)) {
			War3ID gefField = War3ID.fromString("gef" + metaDataField.getId().charAt(3));
			String fieldAsString = gameObject.getFieldAsString(gefField, 0);
			GameObject upgradeEffect = effectIDToUpgradeEffect.get(fieldAsString + metaDataField.getField("effectType"));
			String displayNameOfSubMetaField = upgradeEffect == null ? WE_STRING.WESTRING_ERROR_BADTRIGVAL : upgradeEffect.getField("displayName");
			return String.format(defaultDisplayName, WEString.getString(displayNameOfSubMetaField));
		}
		return defaultDisplayName;
	}

	@Override
	protected String getDisplayPrefix(int level) {
		if (0 < level) {
			String westring = WEString.getString(WE_STRING.WESTRING_AEVAL_LVL);
			return String.format(westring, level) + " - ";
		}
		return "";
	}

	private String getDisplayName2(GameObject metaDataField) {
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";
		String displayName = metaDataField.getField("displayName");
		return prefix + WEString.getString(displayName);
	}
}
