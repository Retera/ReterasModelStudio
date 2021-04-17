package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units.UnitSortByRaceFolder;

public class UnitTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_STANDARDUNITS")),
				new UnitSortByRaceFolder(WEString.getString("WESTRING_UE_CUSTOMUNITS")));
		return root;
	}

}
