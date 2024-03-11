package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableItemData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.items.ItemSortByClassFolder;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Collections;
import java.util.List;

public class ItemTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public ItemTabTreeBrowserBuilder() {
		super(WorldEditorDataType.ITEM);
		unitData = new MutableItemData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("ITEM", "Item");
		customUnitPopupRunner = () -> {};
	}

	protected void setNewUnitData() {
		unitData = new MutableItemData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_STANDARDITEMS")),
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_CUSTOMITEMS")));
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField) {
		return metaDataField.getFieldValue("useItem") > 0;
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
