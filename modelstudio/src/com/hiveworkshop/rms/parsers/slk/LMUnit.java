package com.hiveworkshop.rms.parsers.slk;

import java.util.LinkedHashMap;

public class LMUnit extends Element {

	public LMUnit(final String id, final DataTable table) {
		super(id, table);
		fields = new LinkedHashMap<>();
	}

}
