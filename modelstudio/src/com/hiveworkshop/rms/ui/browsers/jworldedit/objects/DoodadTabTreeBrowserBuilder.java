package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.DoodadFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDoodadData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.DoodadSortByCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

public class DoodadTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	private static final War3ID DOOD_CATEGORY = War3ID.fromString("dcat");
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
//		return new TopLevelCategoryFolder(
//				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_DE_STANDARDDOODS"), "DoodadCategories", DOOD_CATEGORY),
//				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_DE_CUSTOMDOODS"), "DoodadCategories", DOOD_CATEGORY));
	}
}
