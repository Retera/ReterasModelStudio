package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.UnitFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableUnitData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units.UnitSortByRaceFolder;

public class UnitTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	public UnitTabTreeBrowserBuilder(){
		unitData = new MutableUnitData();
		editorFieldBuilder = new UnitFieldBuilder();
		objectTabTreeBrowserBuilder = this;
		editorTabCustomToolbarButtonData = new EditorTabCustomToolbarButtonData("UNIT", "Unit");
		customUnitPopupRunner = new NewCustomUnitDialogRunner(null, unitData);
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
}
