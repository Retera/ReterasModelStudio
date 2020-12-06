package com.hiveworkshop.rms.parsers.slk;

import java.util.Comparator;

public class GameObjectComparator implements Comparator<GameObject> {
	@Override
	public int compare(final GameObject a, final GameObject b) {
		if (a.getField("unitClass").equals("") && !b.getField("unitClass").equals("")) {
			return 1;
		} else if (b.getField("unitClass").equals("") && !a.getField("unitClass").equals("")) {
			return -1;
		}
		final int comp1 = a.getField("unitClass").compareTo(b.getField("unitClass"));
		if (comp1 == 0) {
			final int comp2 = Integer.compare(a.getFieldValue("level"), b.getFieldValue("level"));
			if (comp2 == 0) {
				return a.getName().compareTo(b.getName());
			}
			return comp2;
		}
		return comp1;
	}
}
