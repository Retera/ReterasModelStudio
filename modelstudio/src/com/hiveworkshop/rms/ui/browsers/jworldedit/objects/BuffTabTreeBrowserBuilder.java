package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs.BuffSortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public class BuffTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_STANDARDBUFFS")),
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_CUSTOMBUFFS")));
		return root;
	}

}
