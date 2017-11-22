package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.abilities.AbilitySortByRaceFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;

public class AbilityTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_STANDARDABILS")),
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_CUSTOMABILS")));
		return root;
	}

}
