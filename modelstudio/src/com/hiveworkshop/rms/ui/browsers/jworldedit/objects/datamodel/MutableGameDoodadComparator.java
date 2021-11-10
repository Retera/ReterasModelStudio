package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import java.util.Comparator;

public class MutableGameDoodadComparator implements Comparator<MutableGameObject> {

	@Override
	public int compare(final MutableGameObject a, final MutableGameObject b) {
		if (a.readSLKTag("doodClass").equals("") && !b.readSLKTag("doodClass").equals("")) {
			return 1;
		} else if (b.readSLKTag("doodClass").equals("") && !a.readSLKTag("doodClass").equals("")) {
			return -1;
		}
		final int comp1 = a.readSLKTag("doodClass").compareTo(b.readSLKTag("doodClass"));
		if (comp1 == 0) {
			return a.getName().compareTo(b.getName());
		}
		return comp1;
	}
}
