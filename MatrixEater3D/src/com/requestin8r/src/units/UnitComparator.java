package com.requestin8r.src.units;

import java.util.Comparator;

public class UnitComparator implements Comparator<Unit> {
	@Override
	public int compare(Unit a, Unit b) {
		if( a.getField("unitClass").equals("") && !b.getField("unitClass").equals("") ) {
			return 1;
		}
		else if( b.getField("unitClass").equals("") && !a.getField("unitClass").equals("") ) {
			return -1;
		}
		int comp1 = a.getField("unitClass").compareTo(b.getField("unitClass"));
		if( comp1 == 0 ) {
			int comp2 = new Integer(a.getFieldValue("level")).compareTo(new Integer(b.getFieldValue("level")));
			if( comp2 == 0 )
				return a.getName().compareTo(b.getName());
			return comp2;
		}
		return comp1;
	}
}
