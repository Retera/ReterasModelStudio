package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.upgrades.UpgradeSortByRaceFolder;

public class UpgradeTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_STANDARDUPGRS")),
				new UpgradeSortByRaceFolder(WEString.getString("WESTRING_GE_CUSTOMUPGRS")));
		return root;
	}

}
