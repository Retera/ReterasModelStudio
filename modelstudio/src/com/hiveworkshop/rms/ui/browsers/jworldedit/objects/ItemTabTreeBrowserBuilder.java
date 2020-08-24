package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.items.ItemSortByClassFolder;

public class ItemTabTreeBrowserBuilder implements ObjectTabTreeBrowserBuilder {

	@Override
	public TopLevelCategoryFolder build() {
		final TopLevelCategoryFolder root = new TopLevelCategoryFolder(
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_STANDARDITEMS")),
				new ItemSortByClassFolder(WEString.getString("WESTRING_IE_CUSTOMITEMS")));
		return root;
	}

}
