package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.util.War3ID;

import java.util.Comparator;

public class MutableGameUnitComparator implements Comparator<MutableGameObject> {
	private static final War3ID UNIT_LEVEL = War3ID.fromString("ulev");
	private static final String TAG_NAME = "unitClass";

	@Override
	public int compare(final MutableGameObject a, final MutableGameObject b) {
		String a_slkTag = a.readSLKTag(TAG_NAME);
		String b_slkTag = b.readSLKTag(TAG_NAME);
		if (a_slkTag.equals("") && !b_slkTag.equals("")) {
			return 1;
		} else if (b_slkTag.equals("") && !a_slkTag.equals("")) {
			return -1;
		}
		final int comp1 = a_slkTag.compareTo(b_slkTag);
		if (comp1 == 0) {
			final int comp2 = Integer.compare(a.getFieldAsInteger(UNIT_LEVEL, 0), b.getFieldAsInteger(UNIT_LEVEL, 0));
			if (comp2 == 0) {
				return a.getName().compareTo(b.getName());
			}
			return comp2;
		}
		return comp1;
	}
}
