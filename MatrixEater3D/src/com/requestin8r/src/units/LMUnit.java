package com.requestin8r.src.units;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class LMUnit extends Unit {

	public LMUnit(String id, UnitDataTable table) {
		super(id, table);
		fields = new LinkedHashMap<String,String>();
	}

}
