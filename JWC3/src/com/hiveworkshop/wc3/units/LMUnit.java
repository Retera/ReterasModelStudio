package com.hiveworkshop.wc3.units;

import java.util.LinkedHashMap;

public class LMUnit extends Element {

	public LMUnit(String id, DataTable table) {
		super(id, table);
		fields = new LinkedHashMap<String,String>();
	}

}
