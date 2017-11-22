package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.units.UnitSortByRaceFolder;
import com.hiveworkshop.wc3.resources.WEString;

public class UnitTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_STANDARDUNITS")),
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_CUSTOMUNITS")));
		return root;
	}

}
