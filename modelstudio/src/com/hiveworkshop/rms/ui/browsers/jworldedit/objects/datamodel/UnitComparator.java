package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.parsers.slk.StandardObjectData;

import java.util.Comparator;

public class UnitComparator implements Comparator<StandardObjectData.WarcraftObject> {
	@Override
	public int compare(StandardObjectData.WarcraftObject a, StandardObjectData.WarcraftObject b) {
		if( a.getField("unitClass").equals("") && !b.getField("unitClass").equals("") ) {
			return 1;
		}
		else if( b.getField("unitClass").equals("") && !a.getField("unitClass").equals("") ) {
			return -1;
		}
		int comp1 = a.getField("unitClass").compareTo(b.getField("unitClass"));
		if( comp1 == 0 ) {
			int comp2 = Integer.compare(a.getFieldValue("level"), b.getFieldValue("level"));
			if( comp2 == 0 )
				return a.getName().compareTo(b.getName());
			return comp2;
		}
		return comp1;
	}
}
