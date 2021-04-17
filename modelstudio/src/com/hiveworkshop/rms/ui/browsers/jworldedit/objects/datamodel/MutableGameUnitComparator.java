package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import java.util.Comparator;

import com.hiveworkshop.rms.util.War3ID;

public class MutableGameUnitComparator implements Comparator<MutableObjectData.MutableGameObject> {
	private static final War3ID UNIT_LEVEL = War3ID.fromString("ulev");

	@Override
	public int compare(final MutableObjectData.MutableGameObject a, final MutableObjectData.MutableGameObject b) {
		if (a.readSLKTag("unitClass").equals("") && !b.readSLKTag("unitClass").equals("")) {
			return 1;
		} else if (b.readSLKTag("unitClass").equals("") && !a.readSLKTag("unitClass").equals("")) {
			return -1;
		}
		final int comp1 = a.readSLKTag("unitClass").compareTo(b.readSLKTag("unitClass"));
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
