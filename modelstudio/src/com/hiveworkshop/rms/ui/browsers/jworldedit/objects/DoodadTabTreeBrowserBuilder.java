package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDoodadData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WE_STRING;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.DoodadSortByCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoodadTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {
	protected final War3ID levelField;
	public DoodadTabTreeBrowserBuilder(){
		super(WorldEditorDataType.DOODADS);
		this.levelField = WE_Field.DOODAD_VARIATIONS_FIELD.getId();
		unitData = new MutableDoodadData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("DOOD", "Dood");
		customUnitPopupRunner = () -> {};
	}


	protected void setNewUnitData(){
		unitData = new MutableDoodadData();
	}
	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new DoodadSortByCategoryFolder(WEString.getString("WESTRING_DE_STANDARDDOODS")),
				new DoodadSortByCategoryFolder(WEString.getString("WESTRING_DE_CUSTOMDOODS")));
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

				String prefixedDispName = displayName;

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
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";

		String subPrefix = getSubPrefix(level);
		String displayName = metaDataField.getField("displayName");
		return prefix + subPrefix + WEString.getString(displayName);
	}

	private String getSubPrefix(int level) {
		if (0 < level) {
			String westring = WEString.getString(WE_STRING.WESTRING_DEVAL_VAR);
			return String.format(westring, level) + " - ";
		}
		return "";
	}
}
