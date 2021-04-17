package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortByDoodadCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

public class DestructableTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	private static final War3ID DEST_CATEGORY = War3ID.fromString("bcat");

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_BE_STANDARDDESTS"),
						"DestructibleCategories", DEST_CATEGORY),
				new SortByDoodadCategoryFolder(WEString.getString("WESTRING_BE_CUSTOMDESTS"), "DestructibleCategories",
						DEST_CATEGORY));
		return root;
	}
}
