package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import java.util.Comparator;

public class MutableGameObjectSortStringComparator implements Comparator<MutableGameObject> {

	@Override
	public int compare(final MutableGameObject a, final MutableGameObject b) {
		if (a.readSLKTag("sort").equals("") && !b.readSLKTag("sort").equals("")) {
			return 1;
		} else if (b.readSLKTag("sort").equals("") && !a.readSLKTag("sort").equals("")) {
			return -1;
		}
		final int comp1 = a.readSLKTag("sort").compareTo(b.readSLKTag("sort"));
		if (comp1 == 0) {
			return a.getName().compareTo(b.getName());
		}
		return comp1;
	}
}
