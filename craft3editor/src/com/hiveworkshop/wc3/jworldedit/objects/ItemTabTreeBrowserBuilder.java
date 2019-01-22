package com.hiveworkshop.wc3.jworldedit.objects;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.items.ItemSortByClassFolder;
import com.hiveworkshop.wc3.resources.WEString;

public class ItemTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_STANDARDITEMS")),
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_CUSTOMITEMS")));
		return root;
	}

}
