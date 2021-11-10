package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities.AbilitySortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;

public class AbilityTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		return new TopLevelCategoryFolder(
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_STANDARDABILS")),
				new AbilitySortByRaceFolder(WEString.getString("WESTRING_AE_CUSTOMABILS")));
	}

}
