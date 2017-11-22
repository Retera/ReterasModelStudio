package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.upgrades.UpgradeSortByRaceFolder;
import com.hiveworkshop.wc3.resources.WEString;

public class UpgradeTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_STANDARDUPGRS")),
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_CUSTOMUPGRS")));
		return root;
	}

}
