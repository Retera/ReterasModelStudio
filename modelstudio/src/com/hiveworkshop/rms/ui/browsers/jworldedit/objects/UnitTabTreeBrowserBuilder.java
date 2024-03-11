package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.AbstractObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableUnitData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units.UnitSortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Collections;
import java.util.List;

public class UnitTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public UnitTabTreeBrowserBuilder(){
		super(WorldEditorDataType.UNITS);
		unitData = new MutableUnitData();
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UNIT", "Unit");
		NewCustomUnitDialogRunner dialogRunner = new NewCustomUnitDialogRunner(null, unitData);
		customUnitPopupRunner = dialogRunner::run;
	}

	protected void setNewUnitData(){
		unitData = new MutableUnitData();
	}

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_STANDARDUNITS")),
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_CUSTOMUNITS")));
	}

	@Override
	protected boolean includeField(MutableGameObject gameObject, GameObject metaDataField) {
		return metaDataField.getFieldValue("useUnit") > 0
				|| (gameObject.getFieldAsBoolean(WE_Field.UNIT_IS_BUILDING.getId(), 0) && metaDataField.getFieldValue("useBuilding") > 0)
				|| (Character.isUpperCase(gameObject.getAlias().charAt(0))
				&& metaDataField.getFieldValue("useHero") > 0);
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
