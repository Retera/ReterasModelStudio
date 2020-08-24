package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortByDoodadCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

public class DoodadTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	private static final War3ID DOOD_CATEGORY = War3ID.fromString("dcat");

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_DE_STANDARDDOODS"), "DoodadCategories",
						DOOD_CATEGORY),
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_DE_CUSTOMDOODS"), "DoodadCategories",
						DOOD_CATEGORY));
		return root;
	}
}
