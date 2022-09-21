package com.hiveworkshop.rms.parsers.slk;

import java.util.LinkedHashMap;

public class LMUnit extends Element {

	public LMUnit(final String id, final DataTable parentTable) {
		super(id, parentTable);
		fields = new LinkedHashMap<>();
	}

}
