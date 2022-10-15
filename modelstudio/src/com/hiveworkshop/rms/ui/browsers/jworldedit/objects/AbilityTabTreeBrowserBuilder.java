package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableAbilityData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WE_STRING;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities.AbilitySortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbilityTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {
	protected final War3ID levelField;

	public AbilityTabTreeBrowserBuilder(){
		super(WorldEditorDataType.ABILITIES);
		this.levelField = WE_Field.ABIL_LEVLE.getId();
		unitData = new MutableAbilityData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("ABIL", "Abil");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData(){
		unitData = new MutableAbilityData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_STANDARDABILS")),
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_CUSTOMABILS")));
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField) {
		String useSpecific = metaDataField.getField("useSpecific");
		String notAllowed = metaDataField.getField("notSpecific"); //specificallyNotAllowedAbilityIds

		String codeStringValue = gameObject.getCode().asStringValue();

		boolean doUseSpecific = 0 >= useSpecific.length() || Arrays.asList(useSpecific.split(",")).contains(codeStringValue);
		boolean isAllowed = 0 >= notAllowed.length() || !Arrays.asList(notAllowed.split(",")).contains(codeStringValue);
		if (doUseSpecific && isAllowed) {
			boolean heroAbility = gameObject.getFieldAsBoolean(WE_Field.ABIL_IS_HERO_ABIL.getId(), 0);
			boolean itemAbility = gameObject.getFieldAsBoolean(WE_Field.ABIL_IS_ITEM_ABIL.getId(), 0);
			boolean useHero = heroAbility && !itemAbility && metaDataField.getFieldValue("useHero") == 1;
			boolean useUnit = !heroAbility && !itemAbility && metaDataField.getFieldValue("useUnit") == 1;
			boolean useItem = itemAbility && metaDataField.getFieldValue("useItem") == 1;

			return (useHero || useUnit || useItem);
		} else {
			return false;
		}
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

				AbstractObjectField thing = getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);

				fields.add(thing);
			}
			return fields;
		} else {
			int level = 1 <= repeatCount ? 1 : 0;
			boolean hasMoreThanOneLevel = false;
			int displayLevel = 0;
			String displayName = getDisplayName(metaField, displayLevel, gameObject);
			String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

			String prefixedDispName = displayName;

			AbstractObjectField thing = getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);

			return Collections.singletonList(thing);
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
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";
		String displayName = metaDataField.getField("displayName");
		return prefix + WEString.getString(displayName);
	}

	@Override
	protected String getDisplayPrefix(int level) {
		if (0 < level) {
			String westring = WEString.getString(WE_STRING.WESTRING_AEVAL_LVL);
			return String.format(westring, level) + " - ";
		}
		return "";
	}
}
