package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.buffs.BuffSortByRaceFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;

public class BuffTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_STANDARDBUFFS")),
				new BuffSortByRaceFolder(WEString.getString("WESTRING_FE_CUSTOMBUFFS")));
		return root;
	}

}
