package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.DoodadFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDoodadData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.DoodadSortByCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public class DoodadTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {
	public DoodadTabTreeBrowserBuilder(){
		unitData = new MutableDoodadData();
		editorFieldBuilder = new DoodadFieldBuilder();
		objectTabTreeBrowserBuilder = this;
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
}
