package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.SortByDoodadCategoryFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

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
