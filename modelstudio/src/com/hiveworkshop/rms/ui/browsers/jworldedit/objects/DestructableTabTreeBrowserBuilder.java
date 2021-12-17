package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortByDoodadCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

public class DestructableTabTreeBrowserBuilder extends ObjectTabTreeBrowserBuilder {

	private static final War3ID DESTR_CATEGORY = War3ID.fromString("bcat");

	@Override
	public TopLevelCategoryFolder build() {
		SortByDoodadCategoryFolder standard =
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_BE_STANDARDDESTS"), "DestructibleCategories", DESTR_CATEGORY);
		SortByDoodadCategoryFolder custom =
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_BE_CUSTOMDESTS"), "DestructibleCategories", DESTR_CATEGORY);
		return new TopLevelCategoryFolder(standard, custom);
	}
}
