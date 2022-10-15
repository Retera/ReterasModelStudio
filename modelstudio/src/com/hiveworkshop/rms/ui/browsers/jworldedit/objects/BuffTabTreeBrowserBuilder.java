package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableBuffData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs.BuffSortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Collections;
import java.util.List;

public class BuffTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public BuffTabTreeBrowserBuilder(){
		super(WorldEditorDataType.BUFFS_EFFECTS);
		unitData = new MutableBuffData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("BUFF", "Buff");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData(){
		unitData = new MutableBuffData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_STANDARDBUFFS")),
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_CUSTOMBUFFS")));
	}

	@Override
	protected List<AbstractObjectField> makeFields(War3ID metaKey, GameObject metaField, MutableGameObject gameObject, ObjectData metaData) {
		int level = 0;
		boolean hasMoreThanOneLevel = false;

		int displayLevel = 0;
		String displayName = getDisplayName(metaField, displayLevel, gameObject);
		String rawDataName = metaField.getEditorMetaDataDisplayKey(level);

		String prefixedDispName = displayName;

		AbstractObjectField field = getObjectField(metaKey, level, hasMoreThanOneLevel, metaField, displayName, rawDataName, prefixedDispName);
		return Collections.singletonList(field);
	}
	protected List<AbstractObjectField> makeFields1(War3ID metaKey, GameObject metaField, MutableGameObject gameObject, ObjectData metaData) {
		return Collections.singletonList(create(metaField, gameObject, metaKey, 0, false));
	}


	@Override
	protected String getDisplayName(GameObject metaDataField, int level, MutableGameObject gameObject) {
		String category = metaDataField.getField("category");
		String prefix = categoryName(category) + " - ";
		String displayName = metaDataField.getField("displayName");
		return prefix + WEString.getString(displayName);
	}
}
