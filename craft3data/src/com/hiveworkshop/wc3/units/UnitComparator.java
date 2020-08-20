package com.hiveworkshop.wc3.units;

import java.util.Comparator;

import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftObject;

public class UnitComparator implements Comparator<WarcraftObject> {
	@Override
	public int compare(WarcraftObject a, WarcraftObject b) {
		if( a.getField("unitClass").equals("") && !b.getField("unitClass").equals("") ) {
			return 1;
		}
		else if( b.getField("unitClass").equals("") && !a.getField("unitClass").equals("") ) {
			return -1;
		}
		int comp1 = a.getField("unitClass").compareTo(b.getField("unitClass"));
		if( comp1 == 0 ) {
			int comp2 = Integer.valueOf(a.getFieldValue("level")).compareTo(Integer.valueOf(b.getFieldValue("level")));
			if( comp2 == 0 )
				return a.getName().compareTo(b.getName());
			return comp2;
		}
		return comp1;
	}
}
